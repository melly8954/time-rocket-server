package com.melly.timerocketserver.global.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json; charset=UTF-8");

        String errorMessage;
        if (exception instanceof OAuth2AuthenticationException) {
            // OAuth2Error의 description을 꺼냅니다.
            OAuth2AuthenticationException oauthEx = (OAuth2AuthenticationException) exception;
            errorMessage = oauthEx.getError().getDescription();
        } else {
            // 그 외 AuthenticationException 메시지
            errorMessage = exception.getMessage();
        }

        if (errorMessage == null) {
            errorMessage = "알 수 없는 소셜 로그인 오류가 발생했습니다.";
        }

        String json = String.format("{\"code\":400,\"message\":\"%s\"}", errorMessage);
        response.getWriter().write(json);
    }
}
