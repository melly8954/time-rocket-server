package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.SentChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SentChestRepository extends JpaRepository<SentChestEntity,Long> {
    Page<SentChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId, Pageable pageable);
    Page<SentChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);

    Long countByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId);

    Optional<SentChestEntity> findByIsDeletedFalseAndSentChestIdAndRocketIsNotNullAndRocket_SenderUser_UserId(Long sentChestId, Long userId);


}
