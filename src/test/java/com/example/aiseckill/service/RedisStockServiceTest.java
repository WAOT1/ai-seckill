package com.example.aiseckill.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis 库存服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class RedisStockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisStockService stockService;

    @BeforeEach
    void setUp() throws Exception {
        // 通过反射设置 redisAvailable = true，测试 Redis 分支
        Field field = RedisStockService.class.getDeclaredField("redisAvailable");
        field.setAccessible(true);
        field.set(stockService, true);
    }

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

    @Test
    void testMemoryMode_DeductStock() throws Exception {
        // 切换到内存模式
        Field field = RedisStockService.class.getDeclaredField("redisAvailable");
        field.setAccessible(true);
        field.set(stockService, false);

        // 清理内存存储
        Field memoryField = RedisStockService.class.getDeclaredField("memoryStore");
        memoryField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, String> memoryStore =
            (java.util.concurrent.ConcurrentHashMap<String, String>) memoryField.get(null);
        memoryStore.clear();

        // 初始化库存
        stockService.initStock("stock:2", 5);

        // 扣减库存
        Long result1 = stockService.deductStock("stock:2");
        assertEquals(5L, result1);

        Long result2 = stockService.deductStock("stock:2");
        assertEquals(4L, result2);

        // 库存耗尽
        stockService.initStock("stock:3", 0);
        Long result3 = stockService.deductStock("stock:3");
        assertEquals(0L, result3);

        // 未初始化
        Long result4 = stockService.deductStock("stock:999");
        assertEquals(-1L, result4);
    }
}
