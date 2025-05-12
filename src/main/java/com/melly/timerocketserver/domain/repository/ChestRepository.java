package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.ChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChestRepository extends JpaRepository<ChestEntity, Long> {
    // is_deleted 가 false 인 보관함 로켓 조회
    Page<ChestEntity> findByIsDeletedFalse(Pageable pageable);

    // ChestEntity의 rocket 필드를 통해 RocketEntity에 접근하고, 그 안의 rocketName 필드에 부분 일치 검색을 수행
    // 즉, findBy[isDeletedFalse]And[Rocket_RocketNameContaining] 구조
    Page<ChestEntity> findByIsDeletedFalseAndRocket_RocketNameContaining(String rocketName, Pageable pageable);
}
