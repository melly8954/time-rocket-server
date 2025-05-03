package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.EmailRequest;
import com.melly.timerocketserver.domain.dto.request.EmailVerificationRequest;
import com.melly.timerocketserver.domain.dto.request.PasswordVerificationRequest;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// 1) 주어진 이메일 주소에 대해 6자리 인증 코드를 생성하고 verificationCodes 맵에 저장한다. {이메일 : 인증코드} 형태로 저장될 것이다.
// 2) 입력한 이메일 주소로 발송할 이메일 메시지를 작성한다.
// 3) 2에서 생성한 이메일 메시지를 비동기적으로 발송한다.
// 4) 사용자가 입력한 인증코드와 실제 발송된 인증코드와 일치하는지 확인한다.

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements IMailService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "rkwhr8963@gmail.com";
    private static final Map<String, String> emailCodeMap = new HashMap<>();
    private static final Map<String, TempPasswordEntry> tempPasswordMap = new ConcurrentHashMap<>();

    private static class TempPasswordEntry {
        String password;
        long expireTime; // timestamp
        TempPasswordEntry(String password, long expireTime) {
            this.password = password;
            this.expireTime = expireTime;
        }
    }

    // 8자리 랜덤 코드 생성, static 메서드는 인스턴스(객체)를 생성하지 않아도 호출할 수 있는 메서드
    private static String generateRandomCode() {
        int length = 8;
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    @Override
    public MimeMessage createMail(String email){
        String code = generateRandomCode();    // 객체 생성없이 8자리 랜덤 인증번호 생성
        emailCodeMap.put(email, code);  // 이메일과 인증번호 저장
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("Time Rocket 회원가입 : 이메일 인증번호 발송");
            String body = "<h2>Time Rocket 입니다.<br>환영합니다!</h2><h3>아래의 인증번호를 입력하세요.</h3><h1>" + emailCodeMap.get(email) + "</h1><h3>감사합니다.</h3>" +
                    "<a href='https://www.google.com'>타임로켓 홈페이지</a>";
            helper.setText(body, true);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    @Async
    @Override
    public CompletableFuture<String> sendMail(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        MimeMessage message = createMail(email);
        javaMailSender.send(message);
        return CompletableFuture.completedFuture(emailCodeMap.get(email));
    }

    @Override
    public boolean verifyCode(EmailVerificationRequest emailVerificationRequest) {
        String email = emailVerificationRequest.getEmail();
        String inputCode  = emailVerificationRequest.getCode();
        String storedCode = emailCodeMap.get(email);
        return storedCode != null && storedCode.equals(inputCode);
    }

    @Override
    public String createTemporaryPassword(String email) {
        String tempPassword = generateRandomCode();
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("해당 회원은 존재하지 않습니다.");
        }
        return tempPassword;
    }

    @Override
    public void sendTemporaryPasswordMail(String email, String ignoredTempPassword) {
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
                    + "<h3>반드시 비밀번호를 재설정하세요. 유효기간: 10분</h3>";

            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("임시 비밀번호 전송 오류", e);
        }
    }

    @Override
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
            throw new IllegalArgumentException("해당 회원은 존재하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(inputPassword));
        userRepository.save(user);
        tempPasswordMap.remove(email); // 한 번 사용 후 삭제
    }
}
