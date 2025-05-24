package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.ReceivedChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ReceivedChestPageResponse;
import com.melly.timerocketserver.domain.dto.response.RocketFileResponse;
import com.melly.timerocketserver.domain.entity.ReceivedChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.repository.ReceivedChestRepository;
import com.melly.timerocketserver.global.exception.ChestNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReceivedChestService {
    private final ReceivedChestRepository receivedChestRepository;
    private final DisplayService displayService; // 캐시 갱신용 서비스 의존성 추가

    public ReceivedChestService(ReceivedChestRepository receivedChestRepository, DisplayService displayService) {
        this.receivedChestRepository = receivedChestRepository;
        this.displayService = displayService;
    }

    // 보관함 조회
    public ReceivedChestPageResponse getReceivedChestList(Long userId, String rocketName, Pageable pageable, String receiverType) {
        Page<ReceivedChestEntity> findEntity;

        if (receiverType == null || receiverType.isBlank()) {
            throw new IllegalArgumentException("receiverType 은 필수입니다.");
        }

        if (!receiverType.equals("self") && !receiverType.equals("other") && !receiverType.equals("group")) {
            throw new IllegalArgumentException("receiverType 은 'self', 'other', 'group' 중 하나여야 합니다.");
        }

        if (rocketName == null || rocketName.isEmpty()) {
            findEntity = receivedChestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverType(
                    userId, receiverType, pageable);
        } else {
            findEntity = receivedChestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverTypeAndRocket_RocketNameContaining(
                    userId, receiverType, rocketName, pageable);
        }

        // Page 객체는 Null 이 존재할 수 없음
        if (findEntity.isEmpty()) {
            throw new ChestNotFoundException("해당 조건에 맞는 결과가 없습니다.");
        }

        // 보관함 탭마다 로켓 갯수
        Long receivedCount = receivedChestRepository.countByIsDeletedFalseAndRocket_ReceiverUser_UserId(userId);

        // ChestPageResponse 의 ChestDto 로 변환하여 반환, 자바 스트림 API 사용
        // findEntity.getContent()는 여러 개의 ChestEntity 객체가 담긴 리스트를 반환
        // stream() 메서드는 findEntity.getContent()가 반환하는 객체의 타입인 List<ChestEntity>를 스트림으로 변환하여 각 요소를 처리할 수 있게 만듦
        List<ReceivedChestPageResponse.ReceivedChestDto> receivedChestDtoList = findEntity.getContent().stream()
                .map(find -> ReceivedChestPageResponse.ReceivedChestDto.builder()
                        .receivedChestId(find.getReceivedChestId())
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
    public ReceivedChestDetailResponse getChestDetail(Long userId, Long chestId) {
        ReceivedChestEntity findChest = receivedChestRepository.findByReceivedChestIdAndIsDeletedFalseAndRocket_ReceiverUser_UserId(chestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("본인의 수신 보관함에 해당 로켓이 존재하지 않거나 삭제된 상태입니다."));

        RocketEntity rocket = findChest.getRocket();
        boolean isLocked = findChest.getRocket().getIsLock();
        if(isLocked){
            return ReceivedChestDetailResponse.builder()
                    .rocketId(findChest.getRocket().getRocketId())
                    .rocketName(findChest.getRocket().getRocketName())
                    .designUrl(findChest.getRocket().getDesign())
                    .senderEmail(findChest.getRocket().getSenderUser().getEmail())
                    .sentAt(findChest.getRocket().getSentAt())
                    .lockExpiredAt(findChest.getRocket().getLockExpiredAt())
                    .isLocked(findChest.getRocket().getIsLock())
                    .build();
        }else{
            return ReceivedChestDetailResponse.builder()
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
        ReceivedChestEntity findChest = receivedChestRepository.findByReceivedChestIdAndIsDeletedFalseAndRocket_ReceiverUser_UserId(chestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("본인의 수신 보관함에 해당 로켓이 존재하지 않거나 삭제된 상태입니다."));

        RocketEntity rocket = findChest.getRocket();

        if(rocket.getIsLock()){
            throw new IllegalStateException("해당 로켓은 잠금이 해제되지 않았습니다.");
        }

        boolean willBePublic = !findChest.getIsPublic();

        if (willBePublic) {
            // 공개 처리
            int publicCount = receivedChestRepository.countByRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(userId);

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

        receivedChestRepository.save(findChest);
        // 진열장 캐시 갱신
        displayService.updateDisplayCache(findChest.getRocket().getReceiverUser().getUserId());
    }

    // 로켓 공개 변환 시 작동하는 진열장 배치 저장 메서드
    private Long generateNextDisplayLocation(Long userId) {
        Long maxLocation = receivedChestRepository.findMaxDisplayLocationByUserId(userId);
        Long nextLocation = (maxLocation == null) ? 1L : maxLocation + 1;

        if (nextLocation > 10L) {
            throw new IllegalStateException("진열장에 더 이상 로켓을 배치할 수 없습니다. (최대 10개)");
        }

        return nextLocation;
    }

    // 보관함 로켓 논리 삭제
    @Transactional
    public void softDeleteChest(Long userId, Long chestId) {
        ReceivedChestEntity findChest = receivedChestRepository.findByReceivedChestIdAndIsDeletedFalseAndRocket_ReceiverUser_UserId(chestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("본인의 수신 보관함에 해당 로켓이 존재하지 않거나 삭제된 상태입니다."));
        // 논리 삭제
        if(!findChest.getIsDeleted()){
            findChest.setIsDeleted(true);
            findChest.setDeletedAt(LocalDateTime.now());
            findChest.setDisplayLocation(null);
            findChest.setIsPublic(false);
            findChest.setPublicAt(null);

        }
        receivedChestRepository.save(findChest);
        displayService.updateDisplayCache(findChest.getRocket().getReceiverUser().getUserId());
    }
}
