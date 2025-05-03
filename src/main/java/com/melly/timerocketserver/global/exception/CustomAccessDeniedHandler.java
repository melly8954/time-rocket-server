package com.melly.timerocketserver.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 사용자가 인증은 되었지만, 요청한 리소스에 대한 권한이 없을 때 (403 Forbidden) 호출되는 예외 처리 핸들러
// @Component 등록해서 SecurityConfig 주입
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":403,\"message\":\"접근 권한이 없습니다.\"}");
    }
}
