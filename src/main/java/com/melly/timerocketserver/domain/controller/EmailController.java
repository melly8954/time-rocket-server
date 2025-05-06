package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.EmailRequest;
import com.melly.timerocketserver.domain.dto.request.EmailVerificationRequest;
import com.melly.timerocketserver.domain.dto.request.PasswordVerificationRequest;
import com.melly.timerocketserver.domain.service.IMailService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@EnableAsync
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmailController implements ResponseController {
    private final IMailService mailService;

    // 이메일 인증 코드 발송
    @PostMapping("/emails")
    public CompletableFuture<ResponseEntity<ResponseDto>> mailSend(@RequestBody @Validated EmailRequest emailRequest) {
        return mailService.sendMail(emailRequest)
                .thenApply(code -> {
                    // 인증번호 발송 성공시 응답 생성
                    return makeResponseEntity(HttpStatus.OK, "이메일 인증번호가 발송되었습니다.", null);
                })
                .exceptionally(ex -> {
                    // 예외 발생 시, 구체적인 예외 처리
                    if (ex.getCause() instanceof UserNotFoundException) {
                        return makeResponseEntity(HttpStatus.NOT_FOUND, ex.getCause().getMessage(), null);
                    } else {
                        return makeResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCause().getMessage(), null);
                    }
                });
    }

    // 이메일 인증 코드 검증
    @PostMapping("/emails/verify-code")
    public ResponseEntity<ResponseDto> verifyCode(@RequestBody @Validated EmailVerificationRequest emailVerificationRequest) {
        mailService.verifyCode(emailVerificationRequest);
        return makeResponseEntity(HttpStatus.OK, "인증번호 검증 성공", null);
    }

    // 임시 비밀번호 발급
    @PostMapping("/emails/temp-password")
    public ResponseEntity<ResponseDto> tempPassword(@RequestBody @Validated EmailRequest emailRequest) {
        mailService.processTempPassword(emailRequest.getEmail());
        return makeResponseEntity(HttpStatus.OK, "임시 비밀번호가 이메일로 발송되었습니다.", null);
    }

    // 임시 비밀번호 검증
    @PostMapping("/emails/verify-temporary-password")
    public ResponseEntity<ResponseDto> verifyTemporaryPassword(@RequestBody @Validated PasswordVerificationRequest request) {
        mailService.verifyTemporaryPassword(request);
        return makeResponseEntity(HttpStatus.OK, "임시 비밀번호 인증 및 변경 완료", null);
    }
}
