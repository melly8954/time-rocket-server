package com.melly.timerocketserver.global.security;

import com.melly.timerocketserver.domain.entity.Status;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.global.exception.AccountDeletedException;
import com.melly.timerocketserver.global.exception.AccountInActiveException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import com.melly.timerocketserver.domain.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 로그인 시 사용자 정보를 로드하는 역할
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        // 이메일과 닉네임을 이용해 사용자 검색
        UserEntity userEntity = userService.findByEmailOrNickname(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException("해당 회원은 존재하지 않습니다.");
        }

        // 계정 상태 확인
        if (userEntity.getStatus() == Status.DELETED) {
            throw new AccountDeletedException("탈퇴된 계정입니다. 관리자에게 문의하십시오.");
        }
        if (userEntity.getStatus() == Status.INACTIVE) {
            throw new AccountInActiveException("이 계정은 비활성화 상태입니다. 관리자에게 문의하십시오.");
        }

        return new CustomUserDetails(userEntity);   // CustomUserDetails 는 UserDetails 구현체
    }
}