package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.PasswordRequestDto;
import com.melly.timerocketserver.domain.dto.request.UpdateStatusRequestDto;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.domain.dto.request.SignUpRequestDto;
import com.melly.timerocketserver.domain.service.UserService;
import com.melly.timerocketserver.global.jwt.RefreshService;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated  // @Validated 로 메서드 파라미터 (@PathVariable, @RequestParam)에 직접 붙은 제약을 검사
@RestController
@RequestMapping("/api")
public class UserController implements ResponseController {
    private final UserService userService;
    private final RefreshService refreshService;

    public UserController(UserService userService, RefreshService refreshService) {
        this.userService = userService;
        this.refreshService = refreshService;
    }

    // @Validated 로 DTO 필드 검사
    @PostMapping("/users")
    public ResponseEntity<ResponseDto> signUp(@RequestBody @Validated SignUpRequestDto signUpRequestDto) {
        userService.signUp(signUpRequestDto);  // 예외가 터지면 GlobalExceptionHandler 가 받음
        return makeResponseEntity(HttpStatus.CREATED, "회원가입 성공", null);
    }

    @GetMapping("/users/duplicate-nickname/{nickname}")
    public ResponseEntity<ResponseDto> duplicateNickname(@PathVariable @Pattern(regexp = "^[a-zA-Z0-9]{2,}$", message = "닉네임은 영어 또는 숫자로 구성된 2자 이상이어야 합니다.")
                                                         String nickname) {
        userService.duplicateNickname(nickname);
        return makeResponseEntity(HttpStatus.OK, "중복 체크 완료", null);
    }

    @PostMapping("/tokens/refresh")
    public ResponseEntity<ResponseDto> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        refreshService.reissueToken(request,response);
        return makeResponseEntity(HttpStatus.CREATED, "refresh_token 재발급", null);
    }

    @GetMapping("/users/profile")
    public ResponseEntity<ResponseDto> getUserInfo() {
        // SecurityContextHolder 에서 인증 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 경우
        if (authentication == null || !authentication.isAuthenticated()) {
            return makeResponseEntity(HttpStatus.UNAUTHORIZED, "사용자 인증이 필요합니다.", null);
        }

        // 인증된 사용자 정보 추출
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 정보와 함께 응답 반환
        return makeResponseEntity(HttpStatus.OK, "사용자 인증 완료", principal.getUser());
    }

    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<ResponseDto> updatePassword(@PathVariable @Min(value = 1, message = "userId는 1 이상이어야 합니다.") Long userId,
                                                      @RequestBody @Validated PasswordRequestDto passwordRequestDto){
        userService.updatePassword(userId, passwordRequestDto);
        return makeResponseEntity(HttpStatus.OK, "비밀번호 변경 완료", null);
    }

    // 계정 탈퇴 버튼을 누를 경우 요청
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ResponseDto> updateStatus(@PathVariable @Min(value = 1, message = "userId는 1 이상이어야 합니다.") Long userId,
                                                    @RequestBody UpdateStatusRequestDto updateStatusRequestDto){
        // 회원 탈퇴 (status = DELETED) 요청
        userService.updateStatus(userId, updateStatusRequestDto);
        return makeResponseEntity(HttpStatus.OK, "회원 상태가 변경되었습니다.", null);
    }
}
