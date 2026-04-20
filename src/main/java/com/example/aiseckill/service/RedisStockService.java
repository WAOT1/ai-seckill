package com.example.aiseckill.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RedisStockService {
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    // 内存备用（无Redis时使用）
    private static final ConcurrentHashMap<String, String> memoryStore = new ConcurrentHashMap<>();
    
    private boolean redisAvailable = false;
    
    @PostConstruct
    public void init() {
        try {
            if (redisTemplate != null) {
                redisTemplate.getConnectionFactory().getConnection().ping();
                redisAvailable = true;
                log.info("Redis连接成功，使用Redis模式");
            }
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis不可用，使用内存模式: {}", e.getMessage());
        }
    }
    
    public Long deductStock(String stockKey) {
        if (redisAvailable) {
            return deductFromRedis(stockKey);
        } else {
            return deductFromMemory(stockKey);
        }
    }
    
    private Long deductFromRedis(String stockKey) {
        String script = 
            "local stock = redis.call('get', KEYS[1]); " +
            "if stock == false then return -1; end; " +
            "local num = tonumber(stock); " +
            "if num <= 0 then return 0; end; " +
            "redis.call('decr', KEYS[1]); " +
            "return num;";
        DefaultRedisScript<Long> stockScript = new DefaultRedisScript<>();
        stockScript.setScriptText(script);
        stockScript.setResultType(Long.class);
        return redisTemplate.execute(stockScript, Collections.singletonList(stockKey));
    }
    
    private synchronized Long deductFromMemory(String stockKey) {
        String stock = memoryStore.get(stockKey);
        if (stock == null) return -1L;
        int num = Integer.parseInt(stock);
        if (num <= 0) return 0L;
        memoryStore.put(stockKey, String.valueOf(num - 1));
        return (long) num;
    }
    
    public void initStock(String stockKey, int stock) {
        if (redisAvailable) {
            redisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
        } else {
            memoryStore.put(stockKey, String.valueOf(stock));
        }
    }
    
    public void rollbackStock(String stockKey) {
        if (redisAvailable) {
            redisTemplate.opsForValue().increment(stockKey);
        } else {
            String stock = memoryStore.get(stockKey);
            if (stock != null) {
                memoryStore.put(stockKey, String.valueOf(Integer.parseInt(stock) + 1));
            }
        }
    }
    
    public Long getStock(String stockKey) {
        String stock;
        if (redisAvailable) {
            stock = redisTemplate.opsForValue().get(stockKey);
        } else {
            stock = memoryStore.get(stockKey);
        }
        return stock == null ? -1L : Long.parseLong(stock);
    }
}
