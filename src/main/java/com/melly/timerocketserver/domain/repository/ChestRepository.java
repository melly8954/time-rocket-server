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

    // 보관함 배치 이동 업데이트
    @Modifying // 호출 시 즉시 flush 를 하고 바로 DB에 update 쿼리 반영
    @Query("UPDATE ChestEntity c SET c.chestLocation = :location WHERE c.chestId = :chestId")
    void updateChestLocation(@Param("chestId") Long chestId, @Param("location") Long location);

//
//    // 로켓의 위치(chestLocation)를 가져오는데, 로켓이 특정 사용자에게 속하고, 특정 조건을 만족하는 위치만을 반환
//    @Query("SELECT c.chestLocation " +
//            "FROM ChestEntity c " +
//            "WHERE c.rocket.receiverUser.userId = :userId " +
//            "AND c.chestLocation LIKE :locationPrefix " +
//            "AND c.rocket.receiverType = :receiverType " +
//            "AND c.isDeleted = false")
//    List<String> findChestLocationsByReceiver(@Param("userId") Long userId,
//                                         @Param("locationPrefix") String locationPrefix,
//                                         @Param("receiverType") String receiverType);
//
//    // 특정 회원의 location 에만 중복이 없어야 하므로, 사용자 ID를 기준으로 location 을 찾음
//    Optional<ChestEntity> findByChestLocationAndRocket_ReceiverUser_UserIdAndIsDeletedFalse(String chestLocation, Long userId);
//
//    // 삭제된 로켓 복구
//    Optional<ChestEntity> findByChestIdAndIsDeletedTrue(Long chestId);

    // 보관함에서 is_public = true 와 수신자 userId 조건으로 진열장 조회
    List<ChestEntity> findByIsPublicTrueAndRocket_ReceiverUser_UserId(Long receiverUserId);

    // 진열장 배치
    @Query("SELECT c.displayLocation " +
            "FROM ChestEntity c " +
            "WHERE c.rocket.receiverUser.userId = :userId " +
            "AND c.rocket.receiverType = :receiverType " +
            "AND c.displayLocation LIKE :locationPrefix " +
            "AND c.isPublic = true")
    List<String> findDisplayLocationsByReceiver(@Param("userId") Long userId,
                                                @Param("locationPrefix") String locationPrefix,
                                                @Param("receiverType") String receiverType);

    // 보관함 배치 값 조회
    @Query("SELECT MAX(c.chestLocation) FROM ChestEntity c WHERE c.isDeleted = false")
    Long findMaxChestLocation();
}
