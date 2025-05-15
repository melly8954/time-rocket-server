package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ChestPageResponse;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import com.melly.timerocketserver.global.exception.ChestNotFoundException;
import com.melly.timerocketserver.global.exception.RocketNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public ChestPageResponse getChestList(Long userId, String rocketName, Pageable pageable, String type) {
        Page<ChestEntity> findEntity;
        if (type.equals("received")) {
            if (rocketName == null || rocketName.isEmpty()) {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserId(userId, pageable);
            } else {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_RocketNameContaining(userId, rocketName, pageable);
            }
        } else if (type.equals("sent")) {
            if (rocketName == null || rocketName.isEmpty()) {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_SenderUser_UserId(userId, pageable);
            } else {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(userId, rocketName, pageable);
            }
        } else {
            throw new IllegalArgumentException("type 은 'sent' 또는 'received'여야 합니다.");
        }

        // Page 객체는 Null 이 존재할 수 없음
        if (findEntity.isEmpty()) {
            throw new ChestNotFoundException("해당 조건에 맞는 결과가 없습니다.");
        }


        // 보관함 탭마다 로켓 갯수
        Long receivedCount = chestRepository.countByIsDeletedFalseAndRocket_ReceiverUser_UserId(userId);
        Long sentCount = chestRepository.countByIsDeletedFalseAndRocket_SenderUser_UserId(userId);


        // ChestPageResponse 의 ChestDto 로 변환하여 반환, 자바 스트림 API 사용
        // findEntity.getContent()는 여러 개의 ChestEntity 객체가 담긴 리스트를 반환
        // stream() 메서드는 findEntity.getContent()가 반환하는 객체의 타입인 List<ChestEntity>를 스트림으로 변환하여 각 요소를 처리할 수 있게 만듦
        List<ChestPageResponse.ChestDto> chestDtoList = findEntity.getContent().stream()
                .map(chest -> ChestPageResponse.ChestDto.builder()
                        .chestId(chest.getChestId())
                        .rocketId(chest.getRocket().getRocketId())
                        .rocketName(chest.getRocket().getRocketName())
                        .designUrl(chest.getRocket().getDesign())
                        .senderEmail(chest.getRocket().getSenderUser().getEmail())
                        .receiverNickname(chest.getRocket().getReceiverUser().getNickname())
                        .receiverEmail(chest.getRocket().getReceiverUser().getEmail())
                        .content(chest.getRocket().getContent())
                        .lockExpiredAt(chest.getRocket().getLockExpiredAt())
                        .isPublic(chest.getIsPublic())
                        .publicAt(chest.getPublicAt())
                        .location(chest.getLocation())
                        .build())
                .collect(Collectors.toList());  // 스트림에 담긴 요소들을 하나의 리스트로 모으는 역할

        // 동적으로 정렬 기준과 정렬 방향을 반환
        String sortBy = findEntity.getSort().stream()
                .map(order -> order.getProperty()) // 정렬 기준 필드명만 추출
                .collect(Collectors.joining(","));

        // 동적으로 정렬 방향을 반환
        String sortDirection = findEntity.getSort().stream()
                .map(order -> order.getDirection().name()) // 정렬 방향 추출 (ASC, DESC)
                .collect(Collectors.joining(","));

        return ChestPageResponse.builder()
                .chests(chestDtoList)
                .currentPage(findEntity.getNumber())
                .pageSize(findEntity.getSize())
                .totalElements(findEntity.getTotalElements())
                .totalPages(findEntity.getTotalPages())
                .first(findEntity.isFirst())
                .last(findEntity.isLast())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .receivedCount(receivedCount)
                .sentCount(sentCount)
                .build();
    }
    
    // 보관함 상세조회 메서드
    public ChestDetailResponse getChestDetail(Long userId, Long chestId) {
        ChestEntity findEntity = this.chestRepository.findByChestId(chestId)
                .orElseThrow(()-> new ChestNotFoundException("보관함에 저장된 로켓이 존재하지 않습니다."));

        // 보관함의 로켓 존재 검사
        if(findEntity.getRocket() == null) {
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        // 수신자가 맞는지 확인
        if (!findEntity.getRocket().getReceiverUser().getUserId().equals(userId)) {
            throw new ChestNotFoundException("본인의 보관함만 조회할 수 있습니다.");
        }

        boolean isLocked = findEntity.getRocket().getIsLock();
        if(isLocked){
            ChestDetailResponse detailResponse = ChestDetailResponse.builder()
                    .rocketId(findEntity.getRocket().getRocketId())
                    .rocketName(findEntity.getRocket().getRocketName())
                    .designUrl(findEntity.getRocket().getDesign())
                    .senderEmail(findEntity.getRocket().getSenderUser().getEmail())
                    .sentAt(findEntity.getRocket().getSentAt())
                    .lockExpiredAt(findEntity.getRocket().getLockExpiredAt())
                    .isLocked(findEntity.getRocket().getIsLock())
                    .build();
            return detailResponse;
        }else{
            ChestDetailResponse detailResponse = ChestDetailResponse.builder()
                    .rocketId(findEntity.getRocket().getRocketId())
                    .rocketName(findEntity.getRocket().getRocketName())
                    .designUrl(findEntity.getRocket().getDesign())
                    .senderEmail(findEntity.getRocket().getSenderUser().getEmail())
                    .sentAt(findEntity.getRocket().getSentAt())
                    .content(findEntity.getRocket().getContent())
                    .isLocked(findEntity.getRocket().getIsLock())
                    .build();
            return detailResponse;
        }
    }

    // 위치 이동 처리 메서드
    public void moveRocketLocation(Long chestId, String receiverType, String newLocation) {
        // 최종 이동하려는 위치 문자열 ("self-1-5" 같은 형식)
        String targetLocation = receiverType + "-" + newLocation;

        ChestEntity currentChest = this.chestRepository.findByChestId(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않습니다."));

        if (currentChest.getRocket() == null) {
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        // 현재 로켓의 receiverType 확인
        String currentReceiverType = currentChest.getRocket().getReceiverType();

        // 이동하려는 위치가 같은 receiverType 인지 확인
        if (!receiverType.equals(currentReceiverType)) {
            throw new IllegalStateException("같은 형태의 로켓 수신자 유형끼리는 이동할 수 없습니다.");
        }

        // 이동하려는 위치에 이미 보관함이 존재하는지 확인
        Optional<ChestEntity> chestAtTargetOpt = this.chestRepository.findByLocationAndRocket_ReceiverUser_UserId(
                targetLocation, currentChest.getRocket().getReceiverUser().getUserId()
        );

        if (chestAtTargetOpt.isPresent()) {
            // 해당 위치에 다른 로켓이 있다면 위치를 스왑
            ChestEntity chestAtTarget = chestAtTargetOpt.get();

            // 기존 위치 저장
            String originalLocation = currentChest.getLocation();

            // 위치 교환
            currentChest.setLocation(targetLocation);
            chestAtTarget.setLocation(originalLocation);

            // 저장
            this.chestRepository.save(currentChest);
            this.chestRepository.save(chestAtTarget);
        } else {
            // 해당 위치가 비어 있다면 그냥 이동
            currentChest.setLocation(targetLocation);
            this.chestRepository.save(currentChest);
        }
    }

    // 보관함 공개 여부 변경 메서드
    public void toggleVisibility(Long chestId){
        ChestEntity chest = this.chestRepository.findByChestId(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않습니다."));

        if (chest.getRocket() == null) {
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }

        chest.setIsPublic(!chest.getIsPublic()); // 공개 여부 반전

        if (chest.getIsPublic()) {
            chest.setPublicAt(LocalDateTime.now());
        } else {
            chest.setPublicAt(null);
        }
        this.chestRepository.save(chest);

        // 공개 보관함 캐시 갱신
        this.displayService.updatePublicChestCache(chest.getRocket().getReceiverUser().getUserId());

    }
    
    // 보관함 로켓 논리 삭제
    public void softDeleteChest(Long chestId) {
        ChestEntity findEntity = this.chestRepository.findByChestId(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않습니다."));

        if(findEntity.getRocket() == null){
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }
        // 논리 삭제
        if(!findEntity.getIsDeleted()){
            findEntity.setIsDeleted(true);
            findEntity.setDeletedAt(LocalDateTime.now());
        }
        this.chestRepository.save(findEntity);
    }
}
