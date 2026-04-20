package com.example.aiseckill.service;

import com.example.aiseckill.domain.entity.SeckillOrder;
import com.example.aiseckill.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 秒杀服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class SeckillServiceTest {

    @Mock
    private RedisStockService stockService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SeckillService seckillService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGetSeckillPath() {
        String path = seckillService.getSeckillPath(10001L, 1L);

        assertNotNull(path);
        assertEquals(12, path.length());
    }

    @Test
    void testDoSeckill_Success() {
        when(stockService.deductStock(anyString())).thenReturn(10L);
        when(orderMapper.insert(any(SeckillOrder.class))).thenReturn(1);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
            .thenReturn(true);

        String path = seckillService.getSeckillPath(10001L, 1L);
        when(valueOperations.get(anyString())).thenReturn(path);

        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, path);

        assertTrue(result.isSuccess());
        assertEquals("抢购成功", result.getMessage());
        assertNotNull(result.getOrder());
        assertNotNull(result.getOrder().getOrderNo());
        assertTrue(result.getOrder().getOrderNo().startsWith("SK"));
    }

    @Test
    void testDoSeckill_InvalidPath() {
        when(valueOperations.get(anyString())).thenReturn(null);

        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, "wrongpath");

        assertFalse(result.isSuccess());
        assertEquals("非法请求或链接已过期", result.getMessage());
    }

    @Test
    void testDoSeckill_DuplicateOrder() {
        when(stockService.deductStock(anyString())).thenReturn(10L);
        when(orderMapper.insert(any(SeckillOrder.class))).thenReturn(1);

        String path = seckillService.getSeckillPath(10001L, 1L);
        when(valueOperations.get(anyString())).thenReturn(path);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
            .thenReturn(true)
            .thenReturn(false);

        SeckillService.SeckillResult result1 = seckillService.doSeckill(10001L, 1L, path);
        assertTrue(result1.isSuccess());

        SeckillService.SeckillResult result2 = seckillService.doSeckill(10001L, 1L, path);
        assertFalse(result2.isSuccess());
        assertEquals("您已参与过该活动", result2.getMessage());
    }

    @Test
    void testDoSeckill_StockEmpty() {
        when(stockService.deductStock(anyString())).thenReturn(0L);
        when(valueOperations.get(anyString())).thenReturn("abc123");
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
            .thenReturn(true);

        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, "abc123");

        assertFalse(result.isSuccess());
        assertEquals("商品已售罄", result.getMessage());
    }

    @Test
    void testDoSeckill_StockNotInitialized() {
        when(stockService.deductStock(anyString())).thenReturn(-1L);
        when(valueOperations.get(anyString())).thenReturn("abc123");
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
            .thenReturn(true);

        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, "abc123");

        assertFalse(result.isSuccess());
        assertEquals("活动未开始或商品不存在", result.getMessage());
    }

    @Test
    void testDoSeckill_RollbackOnException() {
        when(stockService.deductStock(anyString())).thenReturn(10L);
        when(orderMapper.insert(any(SeckillOrder.class)))
            .thenThrow(new RuntimeException("数据库异常"));
        when(valueOperations.get(anyString())).thenReturn("abc123");
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
            .thenReturn(true);

        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, "abc123");

        assertFalse(result.isSuccess());
        assertEquals("下单失败，请重试", result.getMessage());
        verify(stockService).rollbackStock(anyString());
    }
}
