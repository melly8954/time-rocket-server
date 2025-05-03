package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.EmailRequest;
import com.melly.timerocketserver.domain.dto.request.EmailVerificationRequest;
import com.melly.timerocketserver.domain.dto.request.PasswordVerificationRequest;
import com.melly.timerocketserver.domain.service.IMailService;
import com.melly.timerocketserver.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@EnableAsync
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmailController {
    private final IMailService mailService;
    private final UserService userService;

    // 인증번호 발송 메소드
    @PostMapping("/emails")
    public CompletableFuture<String> mailSend(@RequestBody EmailRequest emailRequest) {
        return mailService.sendMail(emailRequest)
                .thenApply(number -> String.valueOf(number));
    }

    // 인증번호 검증 메소드
    @PostMapping("/emails/verify-code")
    public String verifyCode(@RequestBody EmailVerificationRequest emailVerificationRequest) {
        boolean isVerified = mailService.verifyCode(emailVerificationRequest);
        return isVerified ? "Verified" : "Verification failed";
    }

    // 임시 비밀번호 재발급 발송 메서드
    @PostMapping("/emails/temp-password")
    public ResponseEntity<String> tempPassword(@RequestBody EmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        if (userService.isEmailExist(email)) {
            String tempPassword = mailService.createTemporaryPassword(email);
            mailService.sendTemporaryPasswordMail(email, tempPassword);
            return ResponseEntity.ok("임시 비밀번호가 이메일로 발송되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 이메일로 가입된 사용자가 없습니다.");
        }
    }

    // 임시 비밀번호 검증 메소드
    @PostMapping("/emails/verify-temporary-password")
    public ResponseEntity<String> verifyTemporaryPassword(@RequestBody PasswordVerificationRequest request) {
        try {
            mailService.verifyTemporaryPassword(request);
            return ResponseEntity.ok("임시 비밀번호 인증 및 변경 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
