package com.melly.timerocketserver.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Spring Security 에서 인증되지 않은 사용자가 보호된 리소스에 접근했을 때 호출되는 예외 처리 핸들러
// @Component 등록해서 SecurityConfig 주입
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":401,\"message\":\"로그인이 필요합니다.\"}");
    }
}
