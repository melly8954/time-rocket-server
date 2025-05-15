package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.RocketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RocketRepository extends JpaRepository<RocketEntity, Long> {
    // 회원 ID 와 임시저장 여부를 통해 로켓 찾기 (userId 는 SenderUser 를 찾을 수 있음)
    Optional<RocketEntity> findBySenderUser_UserIdAndIsTemp(Long userId, boolean isTemp);

    Optional<RocketEntity> findByRocketId(Long rocketId);
}
