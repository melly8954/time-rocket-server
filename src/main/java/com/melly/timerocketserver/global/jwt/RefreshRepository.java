package com.melly.timerocketserver.global.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    Optional<RefreshEntity> findByRefreshToken(String refreshToken);
    Boolean existsByRefreshToken(String refresh_token);

    @Transactional
    void deleteByRefreshToken(String refresh_token);

}
