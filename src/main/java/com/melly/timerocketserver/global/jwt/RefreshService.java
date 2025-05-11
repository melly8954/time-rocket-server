package com.melly.timerocketserver.global.jwt;

import com.melly.timerocketserver.global.exception.JwtException;
import com.melly.timerocketserver.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class RefreshService {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public RefreshService(JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Transactional
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 refresh 토큰 추출
        String refresh_token = extractRefreshTokenFromCookie(request);
        if (refresh_token == null) throw new JwtException("refresh token null");

        // 2. 만료 확인
        if (jwtUtil.isExpired(refresh_token)) {
            throw new JwtException("refresh token expired");
        }

        // 3. 카테고리 확인
        if (!"refresh".equals(jwtUtil.getCategory(refresh_token))) {
            throw new JwtException("invalid refresh token");
        }

        // 4. DB 존재 확인 및 엔티티 조회
        RefreshEntity refreshEntity = refreshRepository.findByRefreshToken(refresh_token)
                .orElseThrow(() -> new JwtException("refresh token not found in repository"));


        // 5. 사용자 정보 추출
        String username = jwtUtil.getUsername(refresh_token);
        String role = jwtUtil.getRole(refresh_token);

        // 6. 토큰 재발급
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        // 7. 기존 엔티티 수정 (delete + save 아님)
        refreshEntity.setRefreshToken(newRefresh);
        refreshEntity.setTokenExpiration(new Date(System.currentTimeMillis() + 86400000L).toString());
        refreshRepository.save(refreshEntity); // 실제로는 save 안 해도 Dirty Checking으로 update됨


        // 8. 응답 세팅
        response.setHeader("Authorization", newAccess);
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh));
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
