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
    // 받은 로켓이 담긴 삭제되지 않은 보관함 목록 조회 (페이징 지원)
    Page<ChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverType(Long userId, String receiverType, Pageable pageable);
    Page<ChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_ReceiverTypeAndRocket_RocketNameContaining(Long userId, String receiverType, String rocketName, Pageable pageable);
    // 받은 로켓이 담긴 삭제되지 않은 보관함 개수 조회 (수신자 기준)
    Long countByIsDeletedFalseAndRocket_ReceiverUser_UserId(Long userId);

    // 보낸 로켓이 담긴 삭제되지 않은 보관함 목록 조회 (페이징 지원)
    Page<ChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId, Pageable pageable);
    Page<ChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);
    //  보낸 로켓이 담긴 삭제되지 않은 보관함 개수 조회 (송신자 기준)
    Long countByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId);

    // 보관함 조회 - 삭제되지 않은 본인 보관함을 chestId로 조회
    Optional<ChestEntity> findByChestIdAndIsDeletedFalseAndRocketIsNotNullAndRocket_ReceiverUser_UserId(Long chestId, Long userId);

    // 보관함 조회 - 삭제되지 않고 잠금이 해제된 보관함을 chestId로 조회 
    Optional<ChestEntity> findByChestIdAndIsDeletedFalseAndRocket_IsLockFalse(Long chestId);
    
    // 삭제된 보관함 조회 - 복구 등을 위한 용도
    Optional<ChestEntity> findByChestIdAndIsDeletedTrue(Long chestId);

    // 진열 중인 보관함 목록 조회 - 수신자 ID 기준으로 isPublic=true, isDeleted=true 상태인 보관함만 반환
    List<ChestEntity> findByIsDeletedFalseAndIsPublicTrueAndRocket_ReceiverUser_UserId(Long receiverUserId);

    // 공개 중인 보관함 수 조회 - 수신자 ID 기준으로 공개 상태이며 삭제되지 않은 보관함 개수 반환
    int countByRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(Long userId);

    // displayLocation 의 최대값 조회 - 수신자 ID 기준, 삭제되지 않은 보관함 대상
    @Query("SELECT MAX(c.displayLocation) FROM ChestEntity c WHERE c.rocket.receiverUser.userId = :userId AND c.isDeleted = false")
    Long findMaxDisplayLocationByUserId(@Param("userId") Long userId);

    // 진열 중인 보관함 조회 - chestId 기준, 삭제되지 않고 공개 상태인 보관함 조회
    Optional<ChestEntity> findByChestIdAndIsDeletedFalseAndIsPublicTrue(Long chestId);

}
