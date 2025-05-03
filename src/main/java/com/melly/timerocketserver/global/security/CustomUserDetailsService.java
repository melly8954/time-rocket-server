package com.melly.timerocketserver.global.security;

import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import com.melly.timerocketserver.domain.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
        UserEntity userEntity = this.userService.findByEmailOrNickname(username);
        if (userEntity != null) {
            return new CustomUserDetails(userEntity);
        } else {
            throw new UserNotFoundException("해당 회원은 존재하지 않습니다.");
        }
    }
}
