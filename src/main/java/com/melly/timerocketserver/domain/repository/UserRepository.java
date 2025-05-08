package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 이메일이 존재하는지 여부
    boolean existsByEmail(String email);
    // 닉네임이 존재하는지 여부
    boolean existsByNickname(String nickname);
    // Email 또는 Nickname 로 UserEntity 찾기
    UserEntity findByEmailOrNickname(String email, String nickname);
    // Email 로 UserEntity 찾기
    UserEntity findByEmail(String email);
    // 암호화된 비밀번호로 UserEntity 찾기
    Optional<UserEntity> findByPassword(String encode);
}
