package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.DisplayDetailResponse;
import com.melly.timerocketserver.domain.dto.response.DisplayDto;
import com.melly.timerocketserver.domain.dto.response.RocketFileResponse;
import com.melly.timerocketserver.domain.entity.ReceivedChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.repository.ReceivedChestRepository;
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
    private final ReceivedChestRepository receivedChestRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DISPLAY_CACHE_KEY_PREFIX = "display:publicChests";

    public DisplayService(ReceivedChestRepository receivedChestRepository, RedisTemplate<String, Object> redisTemplate) {
        this.receivedChestRepository = receivedChestRepository;
        this.redisTemplate = redisTemplate;
    }

    // 공개 보관함 목록 조회 (진열장)
    public List<DisplayDto> getDisplayList(Long userId) {
        String redisKey = DISPLAY_CACHE_KEY_PREFIX + ":" + userId;

        // 1. Redis 캐시 조회
        List<DisplayDto> cachedDisplay = (List<DisplayDto>) this.redisTemplate.opsForValue().get(redisKey);
        if (cachedDisplay != null && !cachedDisplay.isEmpty()) {
            log.info("Redis 캐시에서 진열장 조회 (userId = {})", userId);
            log.info("Display List: {}", cachedDisplay);
            return cachedDisplay;
        }

        // 2. DB에서 공개 보관함 조회 (receiverUser 기준)
        List<ReceivedChestEntity> chestEntities = receivedChestRepository.findByIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(userId);

        // 비어 있으면 404 에러 발생
        if (chestEntities == null || chestEntities.isEmpty()) {
            throw new DisplayNotFoundException("해당 회원의 진열장이 존재하지 않습니다.");
        }

        // Entity → DTO 변환
        List<DisplayDto> displayList = chestEntities.stream()
                .map(DisplayDto::new)
                .collect(Collectors.toList());

        // 3. Redis에 저장 (TTL 5분)
        this.redisTemplate.opsForValue().set(redisKey, displayList, 5, TimeUnit.MINUTES);

        return displayList;
    }

    // 진열장 캐시 갱신용 메서드
    public void updateDisplayCache(Long userId) {
        List<ReceivedChestEntity> chestEntities = receivedChestRepository.findByIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(userId);

        List<DisplayDto> displayList = chestEntities.stream()
                .map(DisplayDto::new)
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
        ReceivedChestEntity findEntity = receivedChestRepository.findByReceivedChestIdAndIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(chestId, userId)
                .orElseThrow(()-> new ChestNotFoundException("본인 진열장에 해당 로켓이 존재하지 않거나 삭제된 상태입니다."));
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
    public void moveLocation(Long sourceChestId, Long targetChestId, Long userId) {
        ReceivedChestEntity source = receivedChestRepository.findByReceivedChestIdAndIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(sourceChestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("이동할 로켓이 진열장에 존재하지 않거나 삭제 상태입니다."));

        ReceivedChestEntity target = receivedChestRepository.findByReceivedChestIdAndIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(targetChestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("이동시킬 위치의 로켓이 진열장에 존재하지 않거나 삭제 상태입니다."));

        // displayLocation 값 교환 또는 이동
        Long sourceLoc = source.getDisplayLocation();
        Long targetLoc = target.getDisplayLocation();

        // 위치 스왑
        source.setDisplayLocation(targetLoc);
        target.setDisplayLocation(sourceLoc);

        receivedChestRepository.save(source);
        receivedChestRepository.save(target);

        // 진열장 캐시 갱신
        updateDisplayCache(userId);
    }
}
