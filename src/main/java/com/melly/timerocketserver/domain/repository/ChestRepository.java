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
    // 받은 로켓 (receiver 기준), 회원별 is_deleted 가 false 인 보관함 로켓 조회
    Page<ChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserId(Long userId, Pageable pageable);
    // 회원별 ChestEntity 의 rocket 필드를 통해 RocketEntity 에 접근하고, 그 안의 rocketName 필드에 부분 일치 검색을 수행
    // 즉, findBy[isDeletedFalse]And[Rocket_RocketNameContaining] 구조
    Page<ChestEntity> findByIsDeletedFalseAndRocket_ReceiverUser_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);
    // 받은 로켓의 개수 (isDeleted = false)
    Long countByIsDeletedFalseAndRocket_ReceiverUser_UserId(Long userId);

    // 보낸 로켓 (sender 기준)
    Page<ChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId, Pageable pageable);
    Page<ChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);
    // 보낸 로켓의 개수 (isDeleted = false)
    Long countByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId);

    // 보관함 상세조회
    Optional<ChestEntity> findByChestId(Long chestId);

    // 로켓의 위치(location)를 가져오는데, 로켓이 특정 사용자에게 속하고, 특정 조건을 만족하는 위치만을 반환
    @Query("SELECT c.location FROM ChestEntity c WHERE c.rocket.receiverUser.userId = :userId AND c.location LIKE :locationPrefix AND c.rocket.receiverType = :receiverType AND c.isDeleted = false")
    List<String> findLocationsByReceiver(@Param("userId") Long userId,
                                         @Param("locationPrefix") String locationPrefix,
                                         @Param("receiverType") String receiverType);

    // 특정 회원의 location 에만 중복이 없어야 하므로, 사용자 ID를 기준으로 location 을 찾음
    Optional<ChestEntity> findByLocationAndRocket_ReceiverUser_UserId(String location, Long userId);

    // 보관함에서 is_public = true 와 수신자 userId 조건으로 진열장 조회
    List<ChestEntity> findByIsPublicTrueAndRocket_ReceiverUser_UserId(Long receiverUserId);
}
