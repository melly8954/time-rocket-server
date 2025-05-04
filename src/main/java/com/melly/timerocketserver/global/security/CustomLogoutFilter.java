package com.melly.timerocketserver.global.security;

import com.melly.timerocketserver.global.jwt.JwtUtil;
import com.melly.timerocketserver.global.jwt.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public CustomLogoutFilter(JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // path and method verify
        String requestUri = request.getRequestURI();
        if (!requestUri.equals("/api/users/logout")) {
            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // get refresh token
        String refresh_token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh_token = cookie.getValue();
                }
            }
        }

        // refresh null check
        if (refresh_token == null) {
            // Refresh 토큰이 없더라도
            // accessToken, JSESSIONID 쿠키는 삭제해줘야 완벽한 로그아웃이 된다
            log.info("로그인 유지 미선택");

            // accessToken 삭제
            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            // 세션 쿠키 삭제
            Cookie sessionCookie = new Cookie("JSESSIONID", null);
            sessionCookie.setMaxAge(0);
            sessionCookie.setPath("/");
            response.addCookie(sessionCookie);

            response.setStatus(HttpServletResponse.SC_OK);
            log.info("로그아웃 성공");
            return;
        }

        // expired check
        try {
            jwtUtil.isExpired(refresh_token);
        } catch (ExpiredJwtException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh_token);
        if (!category.equals("refresh")) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefreshToken(refresh_token);
        if (!isExist) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 로그아웃 진행
        // Refresh 토큰 DB에서 제거
        refreshRepository.deleteByRefreshToken(refresh_token);

        // Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");    // 쿠키가 사이트의 모든 경로에서 유효하도록 설정
        response.addCookie(cookie);

        // accessToken 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0); // 쿠키 만료 시간 0으로 설정
        accessTokenCookie.setPath("/"); // 쿠키가 사이트의 모든 경로에서 유효하도록 설정
        response.addCookie(accessTokenCookie);

        // 세션 쿠키 삭제 (세션 쿠키는 Path나 Domain을 설정하지 않으면 기본적으로 현재 도메인에서만 유효)
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setMaxAge(0); // 쿠키 만료 시간 0으로 설정
        sessionCookie.setPath("/"); // 쿠키가 사이트의 모든 경로에서 유효하도록 설정
        response.addCookie(sessionCookie);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content
        log.info("로그아웃 성공");
    }
}
