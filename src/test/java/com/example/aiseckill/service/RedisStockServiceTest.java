package com.example.aiseckill.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis 库存服务单元测试
 * 生产环境要求 Redis 高可用，测试仅覆盖 Redis 模式
 */
@ExtendWith(MockitoExtension.class)
class RedisStockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisStockService stockService;

    @Test
    void testDeductStock_Success() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
            .thenReturn(10L);

        Long result = stockService.deductStock("stock:1");

        assertEquals(10L, result);
    }

    @Test
    void testDeductStock_Empty() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
            .thenReturn(0L);

        Long result = stockService.deductStock("stock:1");

        assertEquals(0L, result);
    }

    @Test
    void testDeductStock_NotInitialized() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
            .thenReturn(-1L);

        Long result = stockService.deductStock("stock:1");

        assertEquals(-1L, result);
    }

    @Test
    void testInitStock() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        stockService.initStock("stock:1", 100);

        verify(valueOperations).set("stock:1", "100");
    }

    @Test
    void testRollbackStock() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        stockService.rollbackStock("stock:1");

        verify(valueOperations).increment("stock:1");
    }

    @Test
    void testGetStock() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("stock:1")).thenReturn("50");

        Long stock = stockService.getStock("stock:1");

        assertEquals(50L, stock);
    }

    @Test
    void testGetStock_NotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("stock:1")).thenReturn(null);

        Long stock = stockService.getStock("stock:1");

        assertEquals(-1L, stock);
    }
}
