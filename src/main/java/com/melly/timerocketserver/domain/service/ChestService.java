package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ChestPageResponse;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChestService {
    private final ChestRepository chestRepository;

    public ChestService(ChestRepository chestRepository) {
        this.chestRepository = chestRepository;
    }

    public ChestPageResponse getChestList(String rocketName, Pageable pageable) {
        Page<ChestEntity> findEntity = null;
        if (rocketName == null || rocketName.isEmpty()) {
            // rocketName이 비어있다면, isDeleted = false인 항목만 조회
            findEntity = this.chestRepository.findByIsDeletedFalse(pageable);
        } else {
            // rocketName이 존재한다면, 해당 조건을 포함하여 조회
            findEntity = this.chestRepository.findByIsDeletedFalseAndRocket_RocketNameContaining(rocketName, pageable);
        }

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
                .build();
    }

    public ChestDetailResponse getChestDetail(Long chestId) {
        ChestEntity findEntity = this.chestRepository.findByChestId(chestId);
        boolean isLocked = findEntity.getRocket().getIsLock();

        if(isLocked){
            ChestDetailResponse detailResponse = ChestDetailResponse.builder()
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
}
