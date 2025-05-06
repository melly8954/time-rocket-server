package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.EmailRequest;
import com.melly.timerocketserver.domain.dto.request.EmailVerificationRequest;
import com.melly.timerocketserver.domain.dto.request.PasswordVerificationRequest;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements IMailService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;    // 이메일 전송을 담당하는 JavaMailSender
    private static final String senderEmail = "rkwhr8963@gmail.com";    // 발신자 이메일 주소
    private static final Map<String, EmailCodeEntry> emailCodeMap = new ConcurrentHashMap<>();    // 이메일 인증 코드를 저장하는 맵 (이메일을 키로 사용)
    private static final Map<String, TempPasswordEntry> tempPasswordMap = new ConcurrentHashMap<>();    // 임시 비밀번호 정보를 저장하는 맵 (이메일을 키로 사용)

    @Getter
    private static class EmailCodeEntry {   // 이메일 인증코드 정보를 저장하는 Map 의 엔트리
        String code;
        long expireTime;
        public EmailCodeEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
        @Override
        public String toString() {
            return code;  // 인증 코드만 반환
        }
    }

    private static class TempPasswordEntry {    // 임시 비밀번호 정보를 저장하는 Map 의 엔트리
        String password;
        long expireTime;
        TempPasswordEntry(String password, long expireTime) {
            this.password = password;
            this.expireTime = expireTime;
        }
    }

    @Async  // 비동기적으로 이메일을 전송하고 결과를 반환하는 메서드
    @Override
    public CompletableFuture<String> sendMail(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        try {
            if (!userRepository.existsByEmail(email)) {
                // 비동기 전송으로인해 UserNotFoundException 이 발생했을 때, CompletableFuture 실패로 처리
                return CompletableFuture.failedFuture(new UserNotFoundException("해당 이메일로 가입된 사용자가 없습니다."));
            }
            // 이메일 인증코드를 포함한 메일을 생성
            MimeMessage message = createMail(email);
            javaMailSender.send(message);
            return CompletableFuture.completedFuture(emailCodeMap.get(email).getCode());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new RuntimeException("이메일 발송 중 서버 오류 발생", e));
        }
    }

    @Override   // 이메일 인증번호 검증
    public void verifyCode(EmailVerificationRequest emailVerificationRequest) {
        String email = emailVerificationRequest.getEmail();
        String inputCode = emailVerificationRequest.getVerificationCode();
        EmailCodeEntry entry = emailCodeMap.get(email);
        if (entry == null || System.currentTimeMillis() > entry.getExpireTime()) {
            throw new IllegalArgumentException("인증번호가 존재하지 않거나 만료되었습니다.");
        }

        if (!entry.getCode().equals(inputCode)) {
            throw new IllegalArgumentException("유효하지 않은 인증번호입니다.");
        }
    }

    @Override   // 임시 비밀번호를 생성하고 메일로 전송
    public void processTempPassword(String email) {
        // 이메일 존재 여부 확인
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("해당 이메일로 가입된 사용자가 없습니다.");
        }

        // 임시 비밀번호 생성
        String tempPassword = generateRandomCode();
        long expireTime = System.currentTimeMillis() + 10 * 60 * 1000; // 10분
        tempPasswordMap.put(email, new TempPasswordEntry(tempPassword, expireTime));

        // 임시 비밀번호 전송
        sendTemporaryPasswordMail(email, tempPassword);
    }

    @Override   // 임시 비밀번호를 검증
    public void verifyTemporaryPassword(PasswordVerificationRequest passwordVerificationRequest) {
        String email = passwordVerificationRequest.getEmail();
        String inputPassword = passwordVerificationRequest.getTempPassword();

        TempPasswordEntry entry = tempPasswordMap.get(email);
        if (entry == null || System.currentTimeMillis() > entry.expireTime) {
            throw new IllegalArgumentException("임시 비밀번호가 존재하지 않거나 만료되었습니다.");
        }

        if (!entry.password.equals(inputPassword)) {
            throw new IllegalArgumentException("임시 비밀번호가 일치하지 않습니다.");
        }

        // 검증 성공 시 비밀번호 변경
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("해당 회원은 존재하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(inputPassword));
        userRepository.save(user);
        tempPasswordMap.remove(email); // 한 번 사용 후 삭제
    }

    // 8자리 랜덤 코드 생성
    private String generateRandomCode() {
        int length = 8;
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    // 이메일을 생성하고 인증 코드와 함께 메일 메시지를 준비
    private MimeMessage createMail(String email){
        String code = generateRandomCode();    // 8자리 랜덤 인증번호 생성
        long expireAt = System.currentTimeMillis() + 10 * 60 * 1000;
        EmailCodeEntry entry = new EmailCodeEntry(code, expireAt);
        emailCodeMap.put(email, entry); // 이메일과 인증 코드를 맵에 저장
        MimeMessage message = javaMailSender.createMimeMessage();    // 메일 메시지 객체 생성

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);    // 발신자
            helper.setTo(email);    // 수신자
            helper.setSubject("Time Rocket 회원가입 : 이메일 인증번호 발송");    // 제목
            String body = "<h2>Time Rocket 입니다.<br>환영합니다!</h2><h3>아래의 인증번호를 입력하세요. (유효기간: 10분)</h3><h1>" + emailCodeMap.get(email) + "</h1><h3>감사합니다.</h3>" +
                    "<a href='https://www.google.com'>타임로켓 홈페이지</a>";   // 내용
            helper.setText(body, true);
        } catch (MessagingException e) {
            log.error("메일 메시지 생성 실패: {}", e.getMessage());
            throw new IllegalStateException("메일 메시지 생성 실패");
        }

        return message;
    }

    // 임시 비밀번호를 생성하고 메일 메시지와 함께 준비
    private void sendTemporaryPasswordMail(String email, String ignoredTempPassword) {
        // 새 임시 비밀번호 생성 및 Map 저장
        String tempPassword = generateRandomCode();
        long expireTime = System.currentTimeMillis() + 10 * 60 * 1000; // 10분
        tempPasswordMap.put(email, new TempPasswordEntry(tempPassword, expireTime));

        // 메일 발송
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("Time Rocket : 임시 비밀번호 발송");

            String body = "<h2>Time Rocket 입니다.<br>환영합니다!</h2>"
                    + "<p>아래의 임시 비밀번호를 사용하세요.</p>"
                    + "<h1>" + tempPassword + "</h1>"
                    + "<h3>반드시 비밀번호를 재설정하세요. (유효기간: 10분)</h3>";

            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("임시 비밀번호 전송 오류", e);
        }
    }
}
