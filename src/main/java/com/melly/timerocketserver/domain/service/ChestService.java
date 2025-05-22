package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.*;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.entity.RocketSentEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import com.melly.timerocketserver.domain.repository.RocketSentRepository;
import com.melly.timerocketserver.global.exception.ChestNotFoundException;
import com.melly.timerocketserver.global.exception.RocketNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChestService {
    private final RocketSentRepository rocketSentRepository;
    private final ChestRepository chestRepository;
    private final DisplayService displayService; // 캐시 갱신용 서비스 의존성 추가

    public ChestService(RocketSentRepository rocketSentRepository, ChestRepository chestRepository, DisplayService displayService) {
        this.rocketSentRepository = rocketSentRepository;
        this.chestRepository = chestRepository;
        this.displayService = displayService;
    }

    // 보관함 조회
    public Object getChestList(Long userId, String rocketName, Pageable pageable, String type, String receiverType) {
        // 반환할 페이지 객체 변수 (각 타입에 맞게 선언)
        Page<ChestEntity> receivedEntities = null;
        Page<RocketSentEntity> sentEntities = null;

        if (type.equals("received")) {
            // receiverType 체크
            if (receiverType == null || receiverType.isBlank()) {
                throw new IllegalArgumentException("receiverType 은 'received' 타입일 때 필수입니다.");
            }
            if (!receiverType.equals("self") && !receiverType.equals("other") && !receiverType.equals("group")) {
                throw new IllegalArgumentException("receiverType 은 'self', 'other', 'group' 중 하나여야 합니다.");
            }

            // receiverType과 rocketName에 따른 조회
            if (rocketName == null || rocketName.isEmpty()) {
                receivedEntities = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverType(
                        userId, receiverType, pageable);
            } else {
                receivedEntities = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverTypeAndRocket_RocketNameContaining(
                        userId, receiverType, rocketName, pageable);
            }

            // 받은 개수, 보낸 개수 조회
            Long receivedCount = chestRepository.countByIsDeletedFalseAndRocket_ReceiverUser_UserId(userId);
            Long sentCount = rocketSentRepository.countByIsDeletedFalseAndSender_UserId(userId);

            // DTO 변환
            List<ChestPageResponse.ChestDto> chestDtoList = receivedEntities.getContent().stream()
                    .map(this::convertFromChestEntity)
                    .collect(Collectors.toList());

            // 정렬 기준과 방향
            String sortBy = receivedEntities.getSort().stream()
                    .map(order -> order.getProperty())
                    .collect(Collectors.joining(","));
            String sortDirection = receivedEntities.getSort().stream()
                    .map(order -> order.getDirection().name())
                    .collect(Collectors.joining(","));

            // 결과 반환
            return ChestPageResponse.builder()
                    .chests(chestDtoList)
                    .currentPage(receivedEntities.getNumber())
                    .pageSize(receivedEntities.getSize())
                    .totalElements(receivedEntities.getTotalElements())
                    .totalPages(receivedEntities.getTotalPages())
                    .first(receivedEntities.isFirst())
                    .last(receivedEntities.isLast())
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .receivedCount(receivedCount)
                    .sentCount(sentCount)
                    .build();

        } else if (type.equals("sent")) {
            // sent 탭은 RocketSentEntity 기준 조회
            if (rocketName == null || rocketName.isEmpty()) {
                sentEntities = rocketSentRepository.findByIsDeletedFalseAndSender_UserId(userId, pageable);
            } else {
                sentEntities = rocketSentRepository.findByIsDeletedFalseAndSender_UserIdAndRocket_RocketNameContaining(userId, rocketName, pageable);
            }

            // 받은 개수, 보낸 개수 조회
            Long receivedCount = chestRepository.countByIsDeletedFalseAndRocket_ReceiverUser_UserId(userId);
            Long sentCount = rocketSentRepository.countByIsDeletedFalseAndSender_UserId(userId);

            // DTO 변환
            List<SentPageResponse.SentDto> sentDtoList = sentEntities.getContent().stream()
                    .map(this::convertFromRocketSentEntityToSentDto)
                    .collect(Collectors.toList());

            // 정렬 기준과 방향
            String sortBy = sentEntities.getSort().stream()
                    .map(order -> order.getProperty())
                    .collect(Collectors.joining(","));
            String sortDirection = sentEntities.getSort().stream()
                    .map(order -> order.getDirection().name())
                    .collect(Collectors.joining(","));

            // 결과 반환
            return SentPageResponse.builder()
                    .rockets(sentDtoList)
                    .currentPage(sentEntities.getNumber())
                    .pageSize(sentEntities.getSize())
                    .totalElements(sentEntities.getTotalElements())
                    .totalPages(sentEntities.getTotalPages())
                    .first(sentEntities.isFirst())
                    .last(sentEntities.isLast())
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .receivedCount(receivedCount)
                    .sentCount(sentCount)
                    .build();
        } else {
            throw new IllegalArgumentException("type 은 'sent' 또는 'received' 만 가능합니다.");
        }
    }

    // ChestEntity → DTO 변환
    private ChestPageResponse.ChestDto convertFromChestEntity(ChestEntity chest) {
        return ChestPageResponse.ChestDto.builder()
                .chestId(chest.getChestId())
                .rocketId(chest.getRocket().getRocketId())
                .rocketName(chest.getRocket().getRocketName())
                .designUrl(chest.getRocket().getDesign())
                .senderEmail(chest.getRocket().getSenderUser().getEmail())
                .receiverNickname(chest.getRocket().getReceiverUser().getNickname())
                .receiverEmail(chest.getRocket().getReceiverUser().getEmail())
                .content(chest.getRocket().getContent())
                .isLock(chest.getRocket().getIsLock())
                .lockExpiredAt(chest.getRocket().getLockExpiredAt())
                .isPublic(chest.getIsPublic())
                .publicAt(chest.getPublicAt())
                .build();
    }

    // RocketSentEntity → DTO 변환
    private SentPageResponse.SentDto convertFromRocketSentEntityToSentDto(RocketSentEntity sent) {
        RocketEntity rocket = sent.getRocket();
        return SentPageResponse.SentDto.builder()
                .rocketSentId(sent.getRocketSentId())
                .rocketId(rocket.getRocketId())
                .rocketName(rocket.getRocketName())
                .designUrl(rocket.getDesign())
                .senderEmail(sent.getSender().getEmail())
                .receiverNickname(rocket.getReceiverUser().getNickname())
                .receiverEmail(rocket.getReceiverUser().getEmail())
                .content(rocket.getContent())
                .isLock(rocket.getIsLock())
                .lockExpiredAt(rocket.getLockExpiredAt())
                .build();
    }
    
    // 보관함 상세조회 메서드
    public ChestDetailResponse getChestDetail(Long userId, Long chestId) {
        ChestEntity findEntity = chestRepository.findByChestIdAndIsDeletedFalse(chestId)
                .orElseThrow(()-> new ChestNotFoundException("해당 chestId의 보관함이 존재하지 않습니다."));

        // 보관함의 로켓 존재 검사
        if(findEntity.getRocket() == null) {
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        RocketEntity rocket = findEntity.getRocket();
        boolean isLocked = findEntity.getRocket().getIsLock();
        if(isLocked){
            return ChestDetailResponse.builder()
                    .rocketId(findEntity.getRocket().getRocketId())
                    .rocketName(findEntity.getRocket().getRocketName())
                    .designUrl(findEntity.getRocket().getDesign())
                    .senderEmail(findEntity.getRocket().getSenderUser().getEmail())
                    .sentAt(findEntity.getRocket().getSentAt())
                    .lockExpiredAt(findEntity.getRocket().getLockExpiredAt())
                    .isLocked(findEntity.getRocket().getIsLock())
                    .build();
        }else{
            return ChestDetailResponse.builder()
                    .rocketId(findEntity.getRocket().getRocketId())
                    .rocketName(findEntity.getRocket().getRocketName())
                    .designUrl(findEntity.getRocket().getDesign())
                    .senderEmail(findEntity.getRocket().getSenderUser().getEmail())
                    .sentAt(findEntity.getRocket().getSentAt())
                    .content(findEntity.getRocket().getContent())
                    .isLocked(findEntity.getRocket().getIsLock())
                    .rocketFiles(toRocketFileResponseList(rocket.getRocketFiles()))
                    .build();
        }
    }

    // RocketFileEntity 리스트를 RocketFileResponse 리스트로 변환
    private List<RocketFileResponse> toRocketFileResponseList(List<RocketFileEntity> entities) {
        if (entities == null) return null;

        return entities.stream()
                .map(file -> RocketFileResponse.builder()
                        .fileId(file.getFileId())
                        .originalName(file.getOriginalName())
                        .uniqueName(file.getUniqueName())
                        .savedPath(file.getSavedPath())
                        .fileType(file.getFileType())
                        .fileSize(file.getFileSize())
                        .fileOrder(file.getFileOrder())
                        .uploadedAt(file.getUploadedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public RocketSentDetailResponse getSentRocketDetail(Long userId, Long rocketSentId) {
        RocketSentEntity sent = rocketSentRepository.findByRocketSentIdAndSender_UserIdAndIsDeletedFalse(rocketSentId, userId)
                .orElseThrow(() -> new RocketNotFoundException("보낸 로켓 정보를 찾을 수 없습니다."));

        RocketEntity rocket = sent.getRocket();  // Rocket 정보 가져오기
        List<RocketFileEntity> fileEntities = rocket.getRocketFiles();  // Rocket에 연관된 파일들

        return RocketSentDetailResponse.builder()
                .rocketSentId(sent.getRocketSentId())
                .rocketId(rocket.getRocketId())
                .rocketName(rocket.getRocketName())
                .designUrl(rocket.getDesign())
                .senderEmail(sent.getSender().getEmail())
                .sentAt(rocket.getSentAt())
                .content(rocket.getContent())
                .lockExpiredAt(rocket.getLockExpiredAt())
                .isLocked(rocket.getIsLock())
                .rocketFiles(toRocketFileResponseList(fileEntities))  // 파일 목록 변환
                .build();
    }

    @Transactional
    // 보관함 공개 여부 변경 메서드
    public void toggleVisibility(Long chestId){
        ChestEntity chest = chestRepository.findByChestIdAndIsDeletedFalseAndRocket_IsLockFalse(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 chestId의 보관함이 존재하지 않거나, 삭제되었거나, 로켓이 잠금 상태입니다."));

        if (chest.getRocket() == null) {
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        boolean willBePublic = !chest.getIsPublic();

        if (willBePublic) {
            // 현재 공개된 보관함 개수 확인
            Long userId = chest.getRocket().getReceiverUser().getUserId();
            int publicCount = chestRepository.countByRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(userId);

            if (publicCount >= 10) {
                throw new IllegalArgumentException("회원당 진열장에 들어갈 로켓 갯수는 최대 10개입니다.");
            }
            chest.setIsPublic(true);
            chest.setPublicAt(LocalDateTime.now());
            // 공개 시 진열장 위치 배정
            Long displayLoc = generateNextDisplayLocation(userId);
            chest.setDisplayLocation(displayLoc);
        } else {
            // 비공개 처리
            chest.setIsPublic(false);
            chest.setPublicAt(null);
            chest.setDisplayLocation(null);
        }

        chestRepository.save(chest);
        // 진열장 캐시 갱신
        displayService.updateDisplayCache(chest.getRocket().getReceiverUser().getUserId());
    }

    // 로켓 공개 변환 시 작동하는 진열장 배치 저장 메서드
    private Long generateNextDisplayLocation(Long userId) {
        Long maxLocation = chestRepository.findMaxDisplayLocationByUserId(userId);
        Long nextLocation = (maxLocation == null) ? 1L : maxLocation + 1;

        if (nextLocation > 10L) {
            throw new IllegalStateException("진열장에 더 이상 로켓을 배치할 수 없습니다. (최대 10개)");
        }

        return nextLocation;
    }

    // 보관함 로켓 논리 삭제
    @Transactional
    public void softDeleteChest(Long userId, Long chestId) {
        ChestEntity findEntity = chestRepository.findByChestIdAndIsDeletedFalse(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 chestId의 보관함이 존재하지 않거나 삭제된 상태입니다."));

        RocketEntity rocket = findEntity.getRocket();
        if (rocket == null) {
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        // 수신자(userId 기준)인지 송신자(userId 기준)인지 판별
        if (rocket.getReceiverUser().getUserId().equals(userId)) {
            // 수신자 → ChestEntity 논리 삭제
            if (!findEntity.getIsDeleted()) {
                findEntity.setIsDeleted(true);
                findEntity.setDeletedAt(LocalDateTime.now());
                findEntity.setDisplayLocation(null);
                findEntity.setIsPublic(false);
                findEntity.setPublicAt(null);
                chestRepository.save(findEntity);
            }

            // 디스플레이 캐시도 갱신
            displayService.updateDisplayCache(userId);

        } else if (rocket.getSenderUser().getUserId().equals(userId)) {
            // 송신자 → RocketSentEntity 논리 삭제
            RocketSentEntity sent = rocketSentRepository.findByRocketAndSender_UserIdAndIsDeletedFalse(rocket, rocket.getSenderUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("보낸 로켓 정보가 존재하지 않습니다."));

            sent.setIsDeleted(true);
            rocketSentRepository.save(sent);
        } else {
            throw new IllegalArgumentException("해당 사용자는 로켓의 수신자나 송신자가 아닙니다.");
        }
    }

    @Transactional
    public void softDeleteSent(Long userId, Long rocketSentId) {
        RocketSentEntity sent = rocketSentRepository.findByRocketSentIdAndIsDeletedFalse(rocketSentId)
                .orElseThrow(() -> new RuntimeException("보낸 로켓 정보가 존재하지 않거나 삭제된 상태입니다."));

        if (!sent.getSender().getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 사용자는 로켓의 송신자가 아닙니다.");
        }

        if (!sent.getIsDeleted()) {
            sent.setIsDeleted(true);
            rocketSentRepository.save(sent);
        }
    }

    // 삭제된 로켓 복구 메서드
    @Transactional
    public void restoreDeletedChest(Long chestId) {
        ChestEntity findEntity = chestRepository.findByChestIdAndIsDeletedTrue(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않거나 삭제되지 않은 상태입니다."));

        if(findEntity.getRocket() == null){
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }
        // 복구
        if(findEntity.getIsDeleted()){
            findEntity.setIsDeleted(false);
            findEntity.setDeletedAt(null);
        }
        chestRepository.save(findEntity);
    }
}
