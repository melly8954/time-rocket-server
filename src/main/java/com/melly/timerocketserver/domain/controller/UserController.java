package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.domain.dto.request.SignUpRequestDto;
import com.melly.timerocketserver.domain.service.UserService;
import com.melly.timerocketserver.global.jwt.RefreshService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController implements ResponseController {
    private final UserService userService;
    private final RefreshService refreshService;

    public UserController(UserService userService, RefreshService refreshService) {
        this.userService = userService;
        this.refreshService = refreshService;
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseDto> signUp(@RequestBody @Validated SignUpRequestDto signUpRequestDto) {
        this.userService.signUp(signUpRequestDto);  // 예외가 터지면 GlobalExceptionHandler 가 받음
        return makeResponseEntity(HttpStatus.CREATED, "회원가입 성공", null);
    }

    @GetMapping("/users/duplicate-nickname/{nickname}")
    public ResponseEntity<ResponseDto> duplicateNickname(@PathVariable String nickname) {
        this.userService.duplicateNickname(nickname);
        return makeResponseEntity(HttpStatus.OK, "중복 체크 완료", null);
    }

    @PostMapping("/tokens/refresh")
    public ResponseEntity<ResponseDto> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        this.refreshService.reissueToken(request,response);
        return makeResponseEntity(HttpStatus.CREATED, "refresh_token 재발급", null);
    }
}
