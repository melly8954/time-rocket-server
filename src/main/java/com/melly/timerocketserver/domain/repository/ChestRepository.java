package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChestRepository extends JpaRepository<ChestEntity, Long> {
    // 받은 로켓 + receiverType
    Page<ChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverType(Long userId, String receiverType, Pageable pageable);
    Page<ChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverTypeAndRocket_RocketNameContaining(Long userId, String receiverType, String rocketName, Pageable pageable);
    // 받은 로켓의 개수 (isDeleted = false)
    Long countByIsDeletedFalseAndRocket_ReceiverUser_UserId(Long userId);

    // 보낸 로켓 (sender 기준)
    Page<ChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId, Pageable pageable);
    Page<ChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);
    // 보낸 로켓의 개수 (isDeleted = false)
    Long countByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId);

    // 삭제되지 않은 보관함 조회
    Optional<ChestEntity> findByChestIdAndIsDeletedFalse(Long chestId);

    // 삭제된 로켓 복구
    Optional<ChestEntity> findByChestIdAndIsDeletedTrue(Long chestId);

    // 보관함에서 is_public = true 와 수신자 userId 조건으로 진열장 조회
    List<ChestEntity> findByIsPublicTrueAndRocket_ReceiverUser_UserId(Long receiverUserId);

    // 진열장 배치
    @Query("SELECT c.displayLocation FROM ChestEntity c " +
            "WHERE c.rocket.receiverUser.userId = :userId " +
            "AND c.displayLocation LIKE :prefix " +
            "AND c.isDeleted = false")
    List<String> findDisplayLocationsByUserId(@Param("userId") Long userId, @Param("prefix") String prefix);

    // 보관함 isPublic 갯수 조회
    int countByRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(Long userId);
}
