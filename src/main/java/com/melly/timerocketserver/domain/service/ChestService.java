package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ReceivedChestPageResponse;
import com.melly.timerocketserver.domain.dto.response.RocketFileResponse;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import com.melly.timerocketserver.global.exception.ChestAccessDeniedException;
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
    private final ChestRepository chestRepository;
    private final DisplayService displayService; // 캐시 갱신용 서비스 의존성 추가

    public ChestService(ChestRepository chestRepository, DisplayService displayService) {
        this.chestRepository = chestRepository;
        this.displayService = displayService;
    }

    // 보관함 조회
    public ReceivedChestPageResponse getReceivedChestList(Long userId, String rocketName, Pageable pageable, String receiverType) {
        Page<ChestEntity> findEntity;

        if (receiverType == null || receiverType.isBlank()) {
            throw new IllegalArgumentException("receiverType 은 필수입니다.");
        }

        if (!receiverType.equals("self") && !receiverType.equals("other") && !receiverType.equals("group")) {
            throw new IllegalArgumentException("receiverType 은 'self', 'other', 'group' 중 하나여야 합니다.");
        }

        if (rocketName == null || rocketName.isEmpty()) {
            findEntity = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverType(
                    userId, receiverType, pageable);
        } else {
            findEntity = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverTypeAndRocket_RocketNameContaining(
                    userId, receiverType, rocketName, pageable);
        }

        // Page 객체는 Null 이 존재할 수 없음
        if (findEntity.isEmpty()) {
            throw new ChestNotFoundException("해당 조건에 맞는 결과가 없습니다.");
        }

        // 보관함 탭마다 로켓 갯수
        Long receivedCount = chestRepository.countByIsDeletedFalseAndRocket_ReceiverUser_UserId(userId);

        // ChestPageResponse 의 ChestDto 로 변환하여 반환, 자바 스트림 API 사용
        // findEntity.getContent()는 여러 개의 ChestEntity 객체가 담긴 리스트를 반환
        // stream() 메서드는 findEntity.getContent()가 반환하는 객체의 타입인 List<ChestEntity>를 스트림으로 변환하여 각 요소를 처리할 수 있게 만듦
        List<ReceivedChestPageResponse.ReceivedChestDto> receivedChestDtoList = findEntity.getContent().stream()
                .map(find -> ReceivedChestPageResponse.ReceivedChestDto.builder()
                        .chestId(find.getChestId())
                        .rocketId(find.getRocket().getRocketId())
                        .rocketName(find.getRocket().getRocketName())
                        .designUrl(find.getRocket().getDesign())
                        .senderEmail(find.getRocket().getSenderUser().getEmail())
                        .receiverNickname(find.getRocket().getReceiverUser().getNickname())
                        .receiverEmail(find.getRocket().getReceiverUser().getEmail())
                        .content(find.getRocket().getContent())
                        .lockExpiredAt(find.getRocket().getLockExpiredAt())
                        .isPublic(find.getIsPublic())
                        .publicAt(find.getPublicAt())
                        .build())
                .toList();  // 스트림에 담긴 요소들을 하나의 리스트로 모으는 역할

        // 동적으로 정렬 기준과 정렬 방향을 반환
        String sortBy = findEntity.getSort().stream()
                .map(order -> order.getProperty()) // 정렬 기준 필드명만 추출
                .collect(Collectors.joining(","));

        // 동적으로 정렬 방향을 반환
        String sortDirection = findEntity.getSort().stream()
                .map(order -> order.getDirection().name()) // 정렬 방향 추출 (ASC, DESC)
                .collect(Collectors.joining(","));

        return ReceivedChestPageResponse.builder()
                .receivedChests(receivedChestDtoList)
                .currentPage(findEntity.getNumber())
                .pageSize(findEntity.getSize())
                .totalElements(findEntity.getTotalElements())
                .totalPages(findEntity.getTotalPages())
                .first(findEntity.isFirst())
                .last(findEntity.isLast())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .receivedCount(receivedCount)
                .build();
    }
    
    // 보관함 상세조회 메서드
    public ChestDetailResponse getChestDetail(Long userId, Long chestId) {
        ChestEntity findChest = chestRepository.findByChestIdAndIsDeletedFalseAndRocket_ReceiverUser_UserId(chestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("본인의 보관함에 로켓이 존재하지 않거나 삭제된 상태입니다."));

        RocketEntity rocket = findChest.getRocket();
        boolean isLocked = findChest.getRocket().getIsLock();
        if(isLocked){
            return ChestDetailResponse.builder()
                    .rocketId(findChest.getRocket().getRocketId())
                    .rocketName(findChest.getRocket().getRocketName())
                    .designUrl(findChest.getRocket().getDesign())
                    .senderEmail(findChest.getRocket().getSenderUser().getEmail())
                    .sentAt(findChest.getRocket().getSentAt())
                    .lockExpiredAt(findChest.getRocket().getLockExpiredAt())
                    .isLocked(findChest.getRocket().getIsLock())
                    .build();
        }else{
            return ChestDetailResponse.builder()
                    .rocketId(findChest.getRocket().getRocketId())
                    .rocketName(findChest.getRocket().getRocketName())
                    .designUrl(findChest.getRocket().getDesign())
                    .senderEmail(findChest.getRocket().getSenderUser().getEmail())
                    .sentAt(findChest.getRocket().getSentAt())
                    .content(findChest.getRocket().getContent())
                    .isLocked(findChest.getRocket().getIsLock())
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

    // 보관함 공개 여부 변경 메서드
    @Transactional
    public void toggleVisibility(Long userId, Long chestId){
        ChestEntity findChest = chestRepository.findByChestIdAndIsDeletedFalseAndRocket_IsLockFalse(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 chestId의 보관함이 존재하지 않거나, 삭제되었거나, 로켓이 잠금 상태입니다."));

        if (findChest.getRocket() == null) {
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        if (!findChest.getRocket().getReceiverUser().getUserId().equals(userId)) {
            throw new ChestAccessDeniedException("본인의 보관함 로켓만 공개 여부를 변경할 수 있습니다.");
        }

        boolean willBePublic = !findChest.getIsPublic();

        if (willBePublic) {
            // 공개 처리
            int publicCount = chestRepository.countByRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(userId);

            if (publicCount >= 10) {
                throw new IllegalArgumentException("회원당 진열장에 들어갈 로켓 갯수는 최대 10개입니다.");
            }
            findChest.setIsPublic(true);
            findChest.setPublicAt(LocalDateTime.now());
            // 공개 시 진열장 위치 배정
            Long displayLoc = generateNextDisplayLocation(userId);
            findChest.setDisplayLocation(displayLoc);
        } else {
            // 비공개 처리
            findChest.setIsPublic(false);
            findChest.setPublicAt(null);
            findChest.setDisplayLocation(null);
        }

        chestRepository.save(findChest);
        // 진열장 캐시 갱신
        displayService.updateDisplayCache(findChest.getRocket().getReceiverUser().getUserId());
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
        ChestEntity findChest = chestRepository.findByChestIdAndIsDeletedFalseAndRocket_ReceiverUser_UserId(chestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("본인의 보관함에 로켓이 존재하지 않거나 삭제된 상태입니다."));
        // 논리 삭제
        if(!findChest.getIsDeleted()){
            findChest.setIsDeleted(true);
            findChest.setDeletedAt(LocalDateTime.now());
            findChest.setDisplayLocation(null);
            findChest.setIsPublic(false);
            findChest.setPublicAt(null);

        }
        chestRepository.save(findChest);
        displayService.updateDisplayCache(findChest.getRocket().getReceiverUser().getUserId());
    }

    // 삭제된 로켓 복구 메서드 (관리자 용 리펙터링 대상임)
    @Transactional
    public void restoreDeletedChest(Long userId, Long chestId) {
        ChestEntity findChest = chestRepository.findByChestIdAndIsDeletedTrue(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않거나 삭제되지 않은 상태입니다."));

        if(findChest.getRocket() == null){
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        if(!findChest.getRocket().getReceiverUser().getUserId().equals(userId)){
            throw new ChestAccessDeniedException("본인의 보관함 로켓만 복구할 수 있습니다.");

        }
        // 복구
        if(findChest.getIsDeleted()){
            findChest.setIsDeleted(false);
            findChest.setDeletedAt(null);
        }
        chestRepository.save(findChest);
    }
}
