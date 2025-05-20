package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.DisplayDetailResponse;
import com.melly.timerocketserver.domain.dto.response.PublicChestDto;
import com.melly.timerocketserver.domain.dto.response.RocketFileResponse;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import com.melly.timerocketserver.global.exception.ChestNotFoundException;
import com.melly.timerocketserver.global.exception.DisplayNotFoundException;
import com.melly.timerocketserver.global.exception.RocketNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DisplayService {
    private final ChestRepository chestRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DISPLAY_CACHE_KEY_PREFIX = "display:publicChests";

    public DisplayService(ChestRepository chestRepository, RedisTemplate<String, Object> redisTemplate) {
        this.chestRepository = chestRepository;
        this.redisTemplate = redisTemplate;
    }

    // 공개 보관함 목록 조회 (진열장)
    public List<PublicChestDto> getDisplayList(Long userId) {
        String redisKey = DISPLAY_CACHE_KEY_PREFIX + ":" + userId;

        // 1. Redis 캐시 조회
        List<PublicChestDto> cachedDisplay = (List<PublicChestDto>) this.redisTemplate.opsForValue().get(redisKey);
        if (cachedDisplay != null && !cachedDisplay.isEmpty()) {
            log.info("Redis 캐시에서 진열장 조회 (userId = {})", userId);
            log.info("Display List: {}", cachedDisplay);
            return cachedDisplay;
        }

        // 2. DB에서 공개 보관함 조회 (receiverUser 기준)
        List<ChestEntity> chestEntities = chestRepository.findByIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(userId);

        // 비어 있으면 404 에러 발생
        if (chestEntities == null || chestEntities.isEmpty()) {
            throw new DisplayNotFoundException("해당 회원의 진열장이 존재하지 않습니다.");
        }

        // Entity → DTO 변환
        List<PublicChestDto> displayList = chestEntities.stream()
                .map(PublicChestDto::new)
                .collect(Collectors.toList());

        // 3. Redis에 저장 (TTL 5분)
        this.redisTemplate.opsForValue().set(redisKey, displayList, 5, TimeUnit.MINUTES);

        return displayList;
    }

    // 진열장 캐시 갱신용 메서드
    public void updateDisplayCache(Long userId) {
        List<ChestEntity> chestEntities = chestRepository.findByIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(userId);

        List<PublicChestDto> displayList = chestEntities.stream()
                .map(PublicChestDto::new)
                .collect(Collectors.toList());

        String redisKey = DISPLAY_CACHE_KEY_PREFIX + ":" + userId;

        this.redisTemplate.opsForValue().set(
                redisKey,
                displayList,
                5,
                TimeUnit.MINUTES
        );
        log.info("Redis 캐시를 갱신했습니다.");
    }

    public DisplayDetailResponse getDisplayDetail(Long userId, Long chestId) {
        ChestEntity findEntity = chestRepository.findByChestIdAndIsDeletedFalseAndIsPublicTrue(chestId)
                .orElseThrow(()-> new ChestNotFoundException("해당 chestId의 진열장이 존재하지 않습니다."));

        // 진열장의 로켓 존재 검사
        if(findEntity.getRocket() == null) {
            throw new RocketNotFoundException("진열장에 해당 로켓이 존재하지 않습니다.");
        }

        // 수신자가 맞는지 확인
        if (!findEntity.getRocket().getReceiverUser().getUserId().equals(userId)) {
            throw new ChestNotFoundException("본인의 진열장만 조회할 수 있습니다.");
        }

        RocketEntity rocket = findEntity.getRocket();
        return DisplayDetailResponse.builder()
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

    @Transactional
    public void moveLocation(Long sourceChestId, Long targetChestId) {
        ChestEntity source = chestRepository.findByChestIdAndIsDeletedFalseAndIsPublicTrue(sourceChestId)
                .orElseThrow(() -> new ChestNotFoundException("이동할 로켓이 진열장에 존재하지 않거나 삭제 상태입니다."));

        ChestEntity target = chestRepository.findByChestIdAndIsDeletedFalseAndIsPublicTrue(targetChestId)
                .orElseThrow(() -> new ChestNotFoundException("이동시킬 위치의 로켓이 진열장에 존재하지 않거나 삭제 상태입니다."));

        // displayLocation 값 교환 또는 이동
        Long sourceLoc = source.getDisplayLocation();
        Long targetLoc = target.getDisplayLocation();

        // 위치 스왑
        source.setDisplayLocation(targetLoc);
        target.setDisplayLocation(sourceLoc);

        chestRepository.save(source);
        chestRepository.save(target);

        // 진열장 캐시 갱신
        Long userId = source.getRocket().getReceiverUser().getUserId(); // 두 진열장은 같은 회원의 것
        updateDisplayCache(userId);
    }


}
