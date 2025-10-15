package com.github.kevin.oasis.global.web;

import com.github.kevin.oasis.global.oauth.UserInfoHandlerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserInfoHandlerInterceptor userInfoHandlerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInfoHandlerInterceptor)
                .addPathPatterns("/**") // 配置拦截的路径
                .excludePathPatterns("/login", "/error");
    }
}
