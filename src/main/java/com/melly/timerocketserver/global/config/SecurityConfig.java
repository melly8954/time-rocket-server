package com.melly.timerocketserver.global.config;

import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.CustomAccessDeniedHandler;
import com.melly.timerocketserver.global.exception.CustomAuthenticationEntryPoint;
import com.melly.timerocketserver.global.jwt.JwtFilter;
import com.melly.timerocketserver.global.jwt.JwtUtil;
import com.melly.timerocketserver.global.jwt.RefreshRepository;
import com.melly.timerocketserver.global.security.CustomLoginFilter;
import com.melly.timerocketserver.global.security.CustomLogoutFilter;
import com.melly.timerocketserver.global.security.oauth.CustomOAuthFailureHandler;
import com.melly.timerocketserver.global.security.oauth.CustomOAuthUserService;
import com.melly.timerocketserver.global.security.oauth.CustomOAuthSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final CustomOAuthUserService customOAuthUserService;
    private final CustomOAuthSuccessHandler customOAuthSuccessHandler;
    private final CustomOAuthFailureHandler customOAuthFailureHandler;

    public SecurityConfig(UserRepository userRepository, RefreshRepository refreshRepository, AuthenticationConfiguration authenticationConfiguration,
                          JwtUtil jwtUtil, CustomOAuthUserService customOAuth2UserService, CustomOAuthSuccessHandler customOAuthSuccessHandler,
                          CustomOAuthFailureHandler customOAuthFailureHandler) {
        this.userRepository = userRepository;
        this.refreshRepository = refreshRepository;
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.customOAuthUserService = customOAuth2UserService;
        this.customOAuthSuccessHandler = customOAuthSuccessHandler;
        this.customOAuthFailureHandler = customOAuthFailureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean   // Spring Security 에서 인증을 처리하는 핵심 객체로, 사용자가 제공한 자격 증명(예: 아이디, 비밀번호)을 기반으로 인증을 수행
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.
                csrf(csrf ->csrf.disable());
        http
                .formLogin((auth) -> auth.disable());
        http
                .httpBasic((auth) -> auth.disable());       // JWT를 사용하면 클라이언트는 직접 로그인 폼을 사용하지 않고, 토큰을 통해 인증을 진행하기때문에 비활성화
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/api/users","/api/users/login","/api/users/logout",
                                "/api/users/duplicate-nickname/**","/api/users/profile","/api/emails/**").permitAll()
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling((exceptions) -> {
                    exceptions
                            .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                            .accessDeniedHandler(new CustomAccessDeniedHandler());
                });

        // Spring Security 의 필터 체인에 JwtFilter 를 CustomLoginFilter 이전에 추가
        // Jwt 토큰을 사용한 인증을 CustomLoginFilter 보다 먼저 처리
        http
                .addFilterBefore(new JwtFilter(jwtUtil,userRepository), CustomLoginFilter.class);

        // 필터 추가 (UsernamePasswordAuthenticationFilter 를 CustomLoginFilter 로 갈음)
        // CustomLoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        http
                .addFilterAt(new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);

        // 로그아웃 필터 추가 (스프링 시큐리티 로그아웃 필터 앞에 등록)
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        // oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuthUserService))
                        .successHandler(customOAuthSuccessHandler)
                        .failureHandler(customOAuthFailureHandler));

        // JWT를 통한 인증/인가를 위해서 세션을 STATELESS 상태로 설정하는 것이 중요하다.
        // 서버가 세션을 생성하지 않고, 클라이언트의 요청의 헤더에 포함된 JWT 토큰을 통해 인증을 처리한다.
        http.
                sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 교차 출처 리소스 공유(CORS 요청 허용)
        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));    // 프론트 서버
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                        return configuration;
                    }
                })));

        return http.build();
    }
}
