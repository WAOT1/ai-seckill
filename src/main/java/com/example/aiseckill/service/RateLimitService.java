package com.example.aiseckill.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 滑动窗口限流服务
 */
@Slf4j
@Service
public class RateLimitService {
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    /**
     * 滑动窗口限流
     * @param key 限流键
     * @param maxRequests 窗口内最大请求数
     * @param windowSeconds 窗口大小（秒）
     * @return true: 允许通过, false: 限流
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        if (redisTemplate == null) {
            // Redis 不可用时，不做限流（或改内存实现）
            return true;
        }
        
        try {
            long now = System.currentTimeMillis();
            long windowStart = now - (windowSeconds * 1000);
            
            // 使用 Redis ZSet 实现滑动窗口
            // 移除窗口外的记录
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
            
            // 获取当前窗口内的请求数
            Long count = redisTemplate.opsForZSet().zCard(key);
            
            if (count != null && count >= maxRequests) {
                return false;
            }
            
            // 记录当前请求
            redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
            // 设置过期时间（比窗口稍长）
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds + 1));
            
            return true;
        } catch (Exception e) {
            log.error("限流检查失败", e);
            // 限流服务异常时，允许通过（避免误杀）
            return true;
        }
    }
}
