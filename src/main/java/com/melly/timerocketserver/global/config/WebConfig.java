package com.melly.timerocketserver.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 (static 폴더) 매핑
        registry.addResourceHandler("/static/**") // URL 패턴 설정
                .addResourceLocations("classpath:/static/images"); // classpath의 static 폴더 매핑
    }
}
