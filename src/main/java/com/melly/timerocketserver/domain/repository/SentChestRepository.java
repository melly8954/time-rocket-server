package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.SentChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SentChestRepository extends JpaRepository<SentChestEntity,Long> {
    // userId로 isDeleted 가 false 인 sent 로켓 전체 조회 (페이징)
    Page<SentChestEntity> findByIsDeletedFalseAndSender_UserId(Long userId, Pageable pageable);

    // userId, 로켓 이름 포함 조건으로 isDeleted 가 false 인 sent 로켓 조회 (페이징)
    Page<SentChestEntity> findByIsDeletedFalseAndSender_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);

    // userId로 isDeleted 가 false 인 sent 로켓 수 카운트
    Long countByIsDeletedFalseAndSender_UserId(Long userId);

    Optional<SentChestEntity> findByRocketAndSender_UserIdAndIsDeletedFalse(RocketEntity rocket, Long sender);

    Optional<SentChestEntity> findBySentChestIdAndSender_UserIdAndIsDeletedFalse(Long rocketSentId, Long sender);

    Optional<SentChestEntity> findBySentChestIdAndIsDeletedFalse(Long rocketSentId);
}
