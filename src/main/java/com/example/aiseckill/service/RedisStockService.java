package com.example.aiseckill.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;

/**
 * Redis 库存服务
 * 生产环境要求 Redis 高可用（Cluster + Sentinel），不提供内存降级
 */
@Slf4j
@Service
public class RedisStockService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @PostConstruct
    public void init() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("Redis连接成功");
        } catch (Exception e) {
            log.error("Redis连接失败: {}。生产环境请确保 Redis Cluster + Sentinel 高可用", e.getMessage());
            throw new IllegalStateException("Redis 不可用，系统无法启动。请检查 Redis 配置或启动 Redis 服务。", e);
        }
    }
    
    /**
     * 扣减库存（Lua 原子操作）
     * @return 扣减后的库存（>0），库存为0（0），未初始化（-1）
     */
    public Long deductStock(String stockKey) {
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
    
    /**
     * 初始化库存
     */
    public void initStock(String stockKey, int stock) {
        redisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
    }
    
    /**
     * 回滚库存（下单失败时调用）
     */
    public void rollbackStock(String stockKey) {
        redisTemplate.opsForValue().increment(stockKey);
    }
    
    /**
     * 查询库存
     */
    public Long getStock(String stockKey) {
        String stock = redisTemplate.opsForValue().get(stockKey);
        return stock == null ? -1L : Long.parseLong(stock);
    }
}
