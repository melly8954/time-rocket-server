package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.EmailRequest;
import com.melly.timerocketserver.domain.dto.request.EmailVerificationRequest;
import com.melly.timerocketserver.domain.dto.request.PasswordVerificationRequest;
import jakarta.mail.internet.MimeMessage;

import java.util.concurrent.CompletableFuture;

public interface IMailService {
    // 이메일로 전송할 내용 생성
    MimeMessage createMail(String email);
    // createMail() 메서드의 내용을 이메일 전송
    CompletableFuture<String> sendMail(EmailRequest emailRequest);
    // 이메일 인증 코드 검증
    boolean verifyCode(EmailVerificationRequest emailVerificationRequest);

    // 임시 비밀번호 생성
    String createTemporaryPassword(String email);
    // 임시 비밀번호 전송
    void sendTemporaryPasswordMail(String email, String tempPassword);
    // 임시 비밀번호 검증 및 DB 업데이트
    void verifyTemporaryPassword(PasswordVerificationRequest passwordVerificationRequest);

}
