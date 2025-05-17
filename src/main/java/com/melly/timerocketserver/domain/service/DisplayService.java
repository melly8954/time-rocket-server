package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.PublicChestDto;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
        List<ChestEntity> chestEntities = this.chestRepository.findByIsPublicTrueAndRocket_ReceiverUser_UserId(userId);

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
        List<ChestEntity> chestEntities = this.chestRepository.findByIsPublicTrueAndRocket_ReceiverUser_UserId(userId);

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
}
