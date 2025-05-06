package com.melly.timerocketserver.global.security;

import com.melly.timerocketserver.global.jwt.JwtUtil;
import com.melly.timerocketserver.global.jwt.RefreshEntity;
import com.melly.timerocketserver.global.jwt.RefreshRepository;
import com.melly.timerocketserver.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Slf4j
// UsernamePasswordAuthenticationFilter --> 로그인 요청을 처리하고, 인증 토큰을 생성하여 인증 관리
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public CustomLoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        setFilterProcessesUrl("/api/users/login"); // 로그인 URL 변경
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        request.setAttribute("rememberMe", rememberMe); // 성공 핸들러에서 쓰기 위해 저장

        // UsernamePasswordAuthenticationToken --> Spring Security에서 인증을 위한 토큰을 생성하는 객체
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password,null);

        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        // 유저 정보
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // Access Token 생성
        String access = jwtUtil.createJwt("access", username, role, 600000L); // 10분
        response.setHeader("Authorization", access);
        response.setStatus(HttpStatus.OK.value());
        // 로그인 성공시 JSON 응답 작성
        String jsonResponse = "{\n"
                + "\"code\": 200,\n"
                + "\"message\": \"로그인 성공\",\n"
                + "\"data\": "+ access + "\n}";
        response.getWriter().write(jsonResponse);
        log.info("Access Token 생성 성공");

        // rememberMe 여부 확인
        String rememberMe = (String) request.getAttribute("rememberMe");
        if ("true".equals(rememberMe)) {
            // Refresh Token 생성 및 쿠키 설정
            String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L); // 24시간
            Date date = new Date(System.currentTimeMillis() + 86400000L);
            RefreshEntity refreshEntity = RefreshEntity.builder()
                    .username(username)
                    .refreshToken(refresh)
                    .tokenExpiration(date.toString()).build();
            refreshRepository.save(refreshEntity);
            response.addCookie(CookieUtil.createCookie("refresh", refresh));
            log.info("Refresh Token 생성 성공");
        }
    }

    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        // 로그인 실패시 401 응답 코드 반환
        response.setStatus(401);
    }

}
