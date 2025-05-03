package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.SignUpRequestDto;
import com.melly.timerocketserver.domain.entity.Role;
import com.melly.timerocketserver.domain.entity.Status;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.DuplicateNicknameException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입 비즈니스 로직
    public void signUp(SignUpRequestDto signUpRequestDto) {
        // 닉네임과 이메일이 동일한지 확인
        if (signUpRequestDto.getNickname().equalsIgnoreCase(signUpRequestDto.getEmail())) {
            // RuntimeException 은 throws 를 명시하지 않음
            throw new IllegalArgumentException("닉네임은 이메일과 동일할 수 없습니다.");
        }
        // 이메일 중복 검사
        if(userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        // 이메일 중복 검사
        if(userRepository.existsByNickname(signUpRequestDto.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        UserEntity userEntity = UserEntity.builder()
                .email(signUpRequestDto.getEmail())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .nickname(signUpRequestDto.getNickname())
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        this.userRepository.save(userEntity);
    }

    // 닉네임 중복체크 비즈니스 로직
    public void duplicateNickname(String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        if (isDuplicate) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }
    }

    // 이메일과 닉네임을 통한 유저 찾기
    public UserEntity findByEmailOrNickname(String username) {
        UserEntity user = this.userRepository.findByEmailOrNickname(username, username);
        if(user != null){
            return user;
        }else{
            throw new UserNotFoundException("User not found");
        }
    }

    // 이메일을 통한 유저 찾기
    public UserEntity findByEmail(String email) {
        UserEntity user = this.userRepository.findByEmail(email);
        if(user != null){
            return user;
        }else {
            throw new UserNotFoundException("User not found");
        }
    }
}
