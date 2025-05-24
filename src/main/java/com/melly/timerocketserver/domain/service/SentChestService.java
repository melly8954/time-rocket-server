package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.RocketFileResponse;
import com.melly.timerocketserver.domain.dto.response.SentChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.SentChestPageResponse;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.entity.SentChestEntity;
import com.melly.timerocketserver.domain.repository.SentChestRepository;
import com.melly.timerocketserver.global.exception.ChestNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SentChestService {
    private final SentChestRepository sentChestRepository;

    public SentChestService(SentChestRepository sentChestRepository) {
        this.sentChestRepository = sentChestRepository;
    }

    public SentChestPageResponse getSentChestList(Long userId, String rocketName, Pageable pageable){
        Page<SentChestEntity> findEntity;

        // 검색 값에 따른 송신 보관함 목록 조회
        if (rocketName == null || rocketName.isEmpty()) {
            findEntity = sentChestRepository.findByIsDeletedFalseAndRocket_SenderUser_UserId(
                    userId, pageable);
        } else {
            findEntity = sentChestRepository.findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(
                    userId, rocketName, pageable);
        }

        // Page 객체는 Null 이 존재할 수 없음
        if (findEntity.isEmpty()) {
            throw new ChestNotFoundException("해당 조건에 맞는 결과가 없습니다.");
        }

        // 송신 보관함의 아이템 갯수
        Long sentCount = sentChestRepository.countByIsDeletedFalseAndRocket_SenderUser_UserId(userId);

//        SentChestPageResponse sentChestPageResponse = SentChestPageResponse.
        List<SentChestPageResponse.SentChestDto> sentChestDtoList = findEntity.getContent().stream()
                .map(find -> SentChestPageResponse.SentChestDto.builder()
                        .sentChestId(find.getSentChestId())
                        .rocketId(find.getRocket().getRocketId())
                        .rocketName(find.getRocket().getRocketName())
                        .designUrl(find.getRocket().getDesign())
                        .receiverEmail(find.getRocket().getReceiverUser().getEmail())
                        .content(find.getRocket().getContent())
                        .build())
                .toList();

        // 동적으로 정렬 기준과 정렬 방향을 반환
        String sortBy = findEntity.getSort().stream()
                .map(order -> order.getProperty()) // 정렬 기준 필드명만 추출
                .collect(Collectors.joining(","));

        // 동적으로 정렬 방향을 반환
        String sortDirection = findEntity.getSort().stream()
                .map(order -> order.getDirection().name()) // 정렬 방향 추출 (ASC, DESC)
                .collect(Collectors.joining(","));

        return SentChestPageResponse.builder()
                .sentChests(sentChestDtoList)
                .currentPage(findEntity.getNumber())
                .totalElements(findEntity.getTotalElements())
                .totalPages(findEntity.getTotalPages())
                .first(findEntity.isFirst())
                .last(findEntity.isLast())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .sentCount(sentCount)
                .build();
    }

    public SentChestDetailResponse getSentChestDetail(Long userId, Long sentChestId) {
        SentChestEntity findEntity = sentChestRepository.findByIsDeletedFalseAndSentChestIdAndRocketIsNotNullAndRocket_SenderUser_UserId(sentChestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("해당 수신 보관함의 로켓은 존재하지 않거나 삭제된 상태입니다."));

        return SentChestDetailResponse.builder()
                .rocketId(findEntity.getRocket().getRocketId())
                .rocketName(findEntity.getRocket().getRocketName())
                .designUrl(findEntity.getRocket().getDesign())
                .receiverEmail(findEntity.getRocket().getReceiverUser().getEmail())
                .sentAt(findEntity.getRocket().getSentAt())
                .content(findEntity.getRocket().getContent())
                .lockExpiredAt(findEntity.getRocket().getLockExpiredAt())
                .rocketFiles(toRocketFileResponseList(findEntity.getRocket().getRocketFiles()))
                .build();
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

    public void softDeleteSentChest(Long userId, Long sentChestId) {
        SentChestEntity findEntity = sentChestRepository.findByIsDeletedFalseAndSentChestIdAndRocketIsNotNullAndRocket_SenderUser_UserId(sentChestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("해당 수신 보관함의 로켓은 존재하지 않거나 삭제된 상태입니다."));
        // 논리 삭제
        if(!findEntity.getIsDeleted()){
            findEntity.setIsDeleted(true);
            findEntity.setDeletedAt(LocalDateTime.now());
        }
        sentChestRepository.save(findEntity);
    }
}
