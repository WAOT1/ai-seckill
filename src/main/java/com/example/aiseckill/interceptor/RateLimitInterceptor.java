package com.example.aiseckill.interceptor;

import com.example.aiseckill.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 限流拦截器
 * 基于用户 IP + 接口路径进行滑动窗口限流
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Autowired
    private RateLimitService rateLimitService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 每秒最多 5 个请求
    private static final int MAX_REQUESTS = 5;
    private static final int WINDOW_SECONDS = 1;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String uri = request.getRequestURI();
        String key = "rate_limit:" + clientIp + ":" + uri;
        
        boolean allowed = rateLimitService.isAllowed(key, MAX_REQUESTS, WINDOW_SECONDS);
        
        if (!allowed) {
            log.warn("IP {} 访问 {} 触发限流", clientIp, uri);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            
            Map<String, Object> result = Map.of(
                "code", 429,
                "message", "访问过于频繁，请稍后再试",
                "data", null
            );
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }
        
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个 IP 取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
