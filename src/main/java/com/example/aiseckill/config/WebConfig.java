package com.example.aiseckill.config;

import com.example.aiseckill.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流拦截器：只拦截秒杀相关接口
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/seckill/**")
            .excludePathPatterns("/api/seckill/*/path"); // 获取路径本身不限流
    }
}
