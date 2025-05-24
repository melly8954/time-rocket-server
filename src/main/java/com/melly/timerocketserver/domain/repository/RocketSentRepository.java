package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.RocketSentEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RocketSentRepository extends JpaRepository<RocketSentEntity,Long> {
    // userId로 isDeleted가 false인 sent 로켓 전체 조회 (페이징)
    Page<RocketSentEntity> findByIsDeletedFalseAndSender_UserId(Long userId, Pageable pageable);

    // userId, 로켓 이름 포함 조건으로 isDeleted가 false인 sent 로켓 조회 (페이징)
    Page<RocketSentEntity> findByIsDeletedFalseAndSender_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);

    // userId로 isDeleted가 false인 sent 로켓 수 카운트
    Long countByIsDeletedFalseAndSender_UserId(Long userId);

    Optional<RocketSentEntity> findByRocketAndSender_UserIdAndIsDeletedFalse(RocketEntity rocket, Long sender);

    Optional<RocketSentEntity> findByRocketSentIdAndSender_UserIdAndIsDeletedFalse(Long rocketSentId, Long sender);

    Optional<RocketSentEntity> findByRocketSentIdAndIsDeletedFalse(Long rocketSentId);
}
