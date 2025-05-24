package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.ReceivedChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivedChestRepository extends JpaRepository<ReceivedChestEntity, Long> {
    // 삭제되지 않은 수신 보관함 목록 조회 (페이징 지원)
    Page<ReceivedChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverType(Long userId, String receiverType, Pageable pageable);
    Page<ReceivedChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverTypeAndRocket_RocketNameContaining(Long userId, String receiverType, String rocketName, Pageable pageable);
    // 삭제되지 않은 수신 보관함 로켓 개수 조회
    Long countByIsDeletedFalseAndRocket_ReceiverUser_UserId(Long userId);

    // 수신 보관함 조회 - 삭제되지 않았으며, receivedChestId 와 로켓 수신자 ID 기준으로 조회
    Optional<ReceivedChestEntity> findByReceivedChestIdAndIsDeletedFalseAndRocket_ReceiverUser_UserId(Long receivedChestId, Long userId);

    // 수신 보관함 조회 - 로켓 수신자 ID 기준으로 isPublic=true, isDeleted=true 상태인 보관함 조회
    List<ReceivedChestEntity> findByIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(Long receiverUserId);

    // 수신 보관함 개수 조회 - 로켓 수신자 ID 기준으로 공개 상태이며 삭제되지 않은 보관함 개수 반환
    int countByRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(Long userId);

    // displayLocation 의 최대값 조회 - 수신자 ID 기준, 삭제되지 않은 보관함 대상
    @Query("SELECT MAX(c.displayLocation) FROM ReceivedChestEntity c WHERE c.rocket.receiverUser.userId = :userId AND c.isDeleted = false")
    Long findMaxDisplayLocationByUserId(@Param("userId") Long userId);

    // 진열 중인 보관함 조회 - receivedChestId 기준, 삭제되지 않고 공개 상태인 보관함 조회
    Optional<ReceivedChestEntity> findByReceivedChestIdAndIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(Long receivedChestId, Long userId);
}
