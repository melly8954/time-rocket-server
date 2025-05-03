package com.melly.timerocketserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
                        .requestMatchers("/","/api/users","/api/users/login",
                                "/api/users/logout","/api/users/duplicate-nickname/**").permitAll()
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        .anyRequest().authenticated());
        return http.build();
    }
}
