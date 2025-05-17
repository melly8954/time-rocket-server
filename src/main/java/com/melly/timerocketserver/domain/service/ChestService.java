package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ChestPageResponse;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
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
    private final RocketService rocketService;
    private final DisplayService displayService; // 캐시 갱신용 서비스 의존성 추가

    public ChestService(ChestRepository chestRepository, RocketService rocketService, DisplayService displayService) {
        this.chestRepository = chestRepository;
        this.rocketService = rocketService;
        this.displayService = displayService;
    }

    // 보관함 조회
    public ChestPageResponse getChestList(Long userId, String rocketName, Pageable pageable, String type, String receiverType) {
        Page<ChestEntity> findEntity;
        if (type.equals("received")) {
            // receiverType이 없거나 잘못된 값인 경우 예외 처리
            if (receiverType == null || receiverType.isBlank()) {
                throw new IllegalArgumentException("receiverType 은 'received' 타입일 때 필수입니다.");
            }

            if (!receiverType.equals("self") && !receiverType.equals("other") && !receiverType.equals("group")) {
                throw new IllegalArgumentException("receiverType 은 'self', 'other', 'group' 중 하나여야 합니다.");
            }

            // receiverType과 rocketName 여부에 따라 조회
            if (rocketName == null || rocketName.isEmpty()) {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverType(
                        userId, receiverType, pageable);
            } else {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverTypeAndRocket_RocketNameContaining(
                        userId, receiverType, rocketName, pageable);
            }

        } else if (type.equals("sent")) {
            // 보낸 로켓 목록 조회
            if (rocketName == null || rocketName.isEmpty()) {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_SenderUser_UserId(
                        userId, pageable);
            } else {
                findEntity = chestRepository.findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(
                        userId, rocketName, pageable);
            }

        } else {
            throw new IllegalArgumentException("type 은 'sent' 또는 'received' 만 가능합니다.");
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
                        .chestLocation(chest.getChestLocation())
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
        ChestEntity findEntity = this.chestRepository.findByChestIdAndIsDeletedFalse(chestId)
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
                    .build();
        }
    }


    // 위치 이동 처리 메서드
    @Transactional
    public void moveRocketChestLocation(Long sourceChestId, Long targetChestId) {
        ChestEntity sourceChest = chestRepository.findByChestIdAndIsDeletedFalse(sourceChestId)
                .orElseThrow(() -> new ChestNotFoundException("출발지 보관함이 존재하지 않거나 삭제된 상태입니다."));

        ChestEntity targetChest = chestRepository.findByChestIdAndIsDeletedFalse(targetChestId)
                .orElseThrow(() -> new ChestNotFoundException("도착지 보관함이 존재하지 않거나 삭제된 상태입니다."));

        if (sourceChest.getRocket() == null || targetChest.getRocket() == null) {
            throw new RocketNotFoundException("출발지 또는 도착지 보관함에 로켓이 존재하지 않습니다.");
        }

        String sourceReceiverType = sourceChest.getRocket().getReceiverType();
        String targetReceiverType = targetChest.getRocket().getReceiverType();

        if (!sourceReceiverType.equals(targetReceiverType)) {
            throw new IllegalStateException("로켓 수신자 유형이 일치하지 않아 이동할 수 없습니다.");
        }

        Long sourceLocation = sourceChest.getChestLocation();
        Long targetLocation = targetChest.getChestLocation();

        Long tempLocation = -9999999L; // 안전한 임시값

        // 1. source -> 임시값
        chestRepository.updateChestLocation(sourceChestId, tempLocation);

        // 2. target -> source 위치
        chestRepository.updateChestLocation(targetChestId, sourceLocation);

        // 3. source(임시값) -> target 위치
        chestRepository.updateChestLocation(sourceChestId, targetLocation);
    }

//    @Transactional
//    // 보관함 공개 여부 변경 메서드
//    public void toggleVisibility(Long chestId){
//        ChestEntity chest = this.chestRepository.findByChestIdAndIsDeletedFalse(chestId)
//                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않거나 삭제된 상태입니다."));
//
//        if (chest.getRocket() == null) {
//            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
//        }
//
//        // 공개 여부 반전
//        boolean willBePublic = !chest.getIsPublic();
//        chest.setIsPublic(willBePublic);
//
//        if (willBePublic) {
//            chest.setPublicAt(LocalDateTime.now());
//            // 공개 시 진열장 위치 배정
//            String displayLoc = generateDisplayLocation(
//                    chest.getRocket().getReceiverUser().getUserId(),
//                    chest.getRocket().getReceiverType()
//            );
//            chest.setDisplayLocation(displayLoc);
//        } else {
//            chest.setPublicAt(null);
//
//            // 💡 비공개로 변경 시 진열장 위치 제거
//            chest.setDisplayLocation(null);
//        }
//
//        this.chestRepository.save(chest);
//
//        // 진열장 캐시 갱신
//        this.displayService.updateDisplayCache(chest.getRocket().getReceiverUser().getUserId());
//
//    }
//
    // 보관함 로켓 논리 삭제
    public void softDeleteChest(Long chestId) {
        ChestEntity findEntity = this.chestRepository.findByChestIdAndIsDeletedFalse(chestId)
                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않거나 삭제된 상태입니다."));

        if(findEntity.getRocket() == null){
            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
        }
        // 논리 삭제
        if(!findEntity.getIsDeleted()){
            findEntity.setIsDeleted(true);
            findEntity.setDeletedAt(LocalDateTime.now());
            findEntity.setDisplayLocation(null);
            findEntity.setIsPublic(false);
            findEntity.setPublicAt(null);

        }
        this.chestRepository.save(findEntity);
    }
//
//    // 로켓 공개 변환 시 작동하는 진열장 배치 저장 메서드
//    private String generateDisplayLocation(Long userId, String receiverType) {
//        int page = 1;
//        while (true) {
//            String locationPrefix = receiverType + "-" + page + "-%";
//
//            // 💡 displayLocation 기준으로 사용 중인 위치 조회
//            List<String> existingDisplayLocations = chestRepository.findDisplayLocationsByReceiver(userId, locationPrefix, receiverType);
//
//            Set<String> used = new HashSet<>(existingDisplayLocations);
//            List<String> available = new ArrayList<>();
//
//            for (int i = 1; i <= 10; i++) {
//                String loc = receiverType + "-" + page + "-" + i;
//                if (!used.contains(loc)) {
//                    available.add(loc);
//                }
//            }
//
//            if (!available.isEmpty()) {
//                Collections.shuffle(available);
//                return available.get(0);
//            }
//
//            page++;
//        }
//    }

//    @Transactional
//    // 삭제된 로켓 복구 메서드
//    public void restoreDeletedChest(Long chestId) {
//        ChestEntity findEntity = this.chestRepository.findByChestIdAndIsDeletedTrue(chestId)
//                .orElseThrow(() -> new ChestNotFoundException("해당 ID의 보관함이 존재하지 않거나 삭제되지 않은 상태입니다."));
//
//        if(findEntity.getRocket() == null){
//            throw new RocketNotFoundException("보관함에 해당 로켓이 존재하지 않습니다.");
//        }
//        // 복구
//        if(findEntity.getIsDeleted()){
//            findEntity.setIsDeleted(false);
//            findEntity.setDeletedAt(null);
//            findEntity.setChestLocation(rocketService.generateRandomChestLocation(findEntity.getRocket().getReceiverUser().getUserId(),
//                                        findEntity.getRocket().getReceiverType()));
//        }
//        this.chestRepository.save(findEntity);
//    }
}
