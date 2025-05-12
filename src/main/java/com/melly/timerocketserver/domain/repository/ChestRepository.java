package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.ChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChestRepository extends JpaRepository<ChestEntity, Long> {
    // is_deleted 가 false 인 보관함 로켓 조회
    Page<ChestEntity> findByIsDeletedFalse(Pageable pageable);

    // ChestEntity의 rocket 필드를 통해 RocketEntity에 접근하고, 그 안의 rocketName 필드에 부분 일치 검색을 수행
    // 즉, findBy[isDeletedFalse]And[Rocket_RocketNameContaining] 구조
    Page<ChestEntity> findByIsDeletedFalseAndRocket_RocketNameContaining(String rocketName, Pageable pageable);

    // 보관함 상세조회
    Optional<ChestEntity> findByChestId(Long chestId);

    // 로켓 전송 시 보관함에 생성되는 배치설정 - receiverType에 따른 위치 필터링
    @Query("SELECT c.location FROM ChestEntity c WHERE c.rocket.receiverUser.userId = :userId AND c.location LIKE :locationPrefix AND c.rocket.receiverType = :receiverType AND c.isDeleted = false")
    List<String> findLocationsByReceiver(@Param("userId") Long userId,
                                         @Param("locationPrefix") String locationPrefix,
                                         @Param("receiverType") String receiverType);

}
