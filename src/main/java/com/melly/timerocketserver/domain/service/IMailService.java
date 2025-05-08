package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.EmailRequest;
import com.melly.timerocketserver.domain.dto.request.EmailVerificationRequest;
import com.melly.timerocketserver.domain.dto.request.PasswordVerificationRequest;

import java.util.concurrent.CompletableFuture;

public interface IMailService {
    // 이메일 전송 메서드
    CompletableFuture<String> sendMail(EmailRequest emailRequest);
    // 이메일 인증 코드 검증
    void verifyCode(EmailVerificationRequest emailVerificationRequest);

    // 임시 비밀번호 생성 및 처리
    void processTempPassword(String email);
    // 임시 비밀번호 검증 및 DB 업데이트
    void verifyTemporaryPassword(PasswordVerificationRequest passwordVerificationRequest);

}
