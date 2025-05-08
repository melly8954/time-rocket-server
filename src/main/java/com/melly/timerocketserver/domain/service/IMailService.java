package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.EmailRequestDto;
import com.melly.timerocketserver.domain.dto.request.EmailVerificationRequestDto;
import com.melly.timerocketserver.domain.dto.request.PasswordVerificationRequestDto;

import java.util.concurrent.CompletableFuture;

public interface IMailService {
    // 이메일 전송 메서드
    CompletableFuture<String> sendMail(EmailRequestDto emailRequestDto);
    // 이메일 인증 코드 검증
    void verifyCode(EmailVerificationRequestDto emailVerificationRequestDto);

    // 임시 비밀번호 생성 및 처리
    void processTempPassword(String email);
    // 임시 비밀번호 검증 및 DB 업데이트
    void verifyTemporaryPassword(PasswordVerificationRequestDto passwordVerificationRequestDto);

}
