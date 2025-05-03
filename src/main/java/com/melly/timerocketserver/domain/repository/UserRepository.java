package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByNickname(String nickname);
    UserEntity findByEmailOrNickname(String email, String nickname);
    UserEntity findByEmail(String email);
}
