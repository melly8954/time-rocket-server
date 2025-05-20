package com.melly.timerocketserver.global.security.oauth;

import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.domain.service.UserService;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import com.melly.timerocketserver.global.jwt.JwtUtil;
import com.melly.timerocketserver.global.jwt.RefreshEntity;
import com.melly.timerocketserver.global.jwt.RefreshRepository;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.global.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component
public class CustomOAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;

    public CustomOAuthSuccessHandler(JwtUtil jwtUtil, RefreshRepository refreshRepository, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String role = customUserDetails.getUser().getRoleDescription();

        // 로그인 후 마지막 로그인 시간 업데이트
        updateLastLogin(username);

        String access = jwtUtil.createJwt("access", username, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);       // 24시간

        Date date = new Date(System.currentTimeMillis() + 86400000L);
        RefreshEntity refreshEntity = RefreshEntity.builder()
                .username(username)
                .refreshToken(refresh)
                .tokenExpiration(date.toString()).build();
        refreshRepository.save(refreshEntity);

        // 쿠키 생성
        Cookie accessTokenCookie = new Cookie("accessToken", access);
        accessTokenCookie.setPath("/");               // 쿠키가 적용될 경로 ("/"는 모든 경로에 적용)
        accessTokenCookie.setMaxAge(60 * 60);         // 쿠키 만료 시간 (초 단위, 여기서는 1시간)

        // 응답 설정
        response.addCookie(accessTokenCookie);
        response.addCookie(CookieUtil.createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());

        // 소셜 로그인(OAuth2)이 성공한 후, 백엔드(Spring Security)가 프론트엔드 페이지로 리디렉션시키는 코드
        response.sendRedirect("http://localhost:5173/oauth/redirect");

    }

    // 마지막 로그인 시간 업데이트 메소드
    private void updateLastLogin(String username) {
        // 여기에 DB에서 사용자 정보를 조회한 후, last_login_at 필드를 현재 시간으로 갱신하는 로직 추가
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("해당 회원은 존재하지 않습니다."));
        // 현재 시간으로 last_login_at 업데이트
        userEntity.setLastLoginAt(LocalDateTime.now());
        userRepository.save(userEntity);
        log.info("유저의 마지막 로그인 시간을 업데이트했습니다.");
    }
}