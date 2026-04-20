package com.example.aiseckill.service;

import com.example.aiseckill.domain.entity.SeckillOrder;
import com.example.aiseckill.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 秒杀服务单元测试
 * 纯内存测试，不依赖 Redis
 */
@ExtendWith(MockitoExtension.class)
class SeckillServiceTest {

    @Mock
    private RedisStockService stockService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private SeckillService seckillService;

    @BeforeEach
    void setUp() throws Exception {
        // 清理静态内存存储，避免测试间状态污染
        Field memoryField = SeckillService.class.getDeclaredField("memoryStore");
        memoryField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, String> memoryStore =
            (java.util.concurrent.ConcurrentHashMap<String, String>) memoryField.get(null);
        memoryStore.clear();
    }

    @Test
    void testGetSeckillPath() {
        String path = seckillService.getSeckillPath(10001L, 1L);

        assertNotNull(path);
        assertEquals(12, path.length());
    }

    @Test
    void testDoSeckill_Success() {
        // 模拟库存充足
        when(stockService.deductStock(anyString())).thenReturn(10L);
        when(orderMapper.insert(any(SeckillOrder.class))).thenReturn(1);

        // 先获取 path
        String path = seckillService.getSeckillPath(10001L, 1L);

        // 执行秒杀
        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, path);

        assertTrue(result.isSuccess());
        assertEquals("抢购成功", result.getMessage());
        assertNotNull(result.getOrder());
        assertNotNull(result.getOrder().getOrderNo());
        assertTrue(result.getOrder().getOrderNo().startsWith("SK"));
    }

    @Test
    void testDoSeckill_InvalidPath() {
        // 使用错误的 path
        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, "wrongpath");

        assertFalse(result.isSuccess());
        assertEquals("非法请求或链接已过期", result.getMessage());
    }

    @Test
    void testDoSeckill_DuplicateOrder() {
        when(stockService.deductStock(anyString())).thenReturn(10L);
        when(orderMapper.insert(any(SeckillOrder.class))).thenReturn(1);

        String path = seckillService.getSeckillPath(10001L, 1L);

        // 第一次下单成功
        SeckillService.SeckillResult result1 = seckillService.doSeckill(10001L, 1L, path);
        assertTrue(result1.isSuccess());

        // 第二次下单失败（幂等）
        String path2 = seckillService.getSeckillPath(10001L, 1L);
        SeckillService.SeckillResult result2 = seckillService.doSeckill(10001L, 1L, path2);
        assertFalse(result2.isSuccess());
        assertEquals("您已参与过该活动", result2.getMessage());
    }

    @Test
    void testDoSeckill_StockEmpty() {
        // 模拟库存为0
        when(stockService.deductStock(anyString())).thenReturn(0L);

        String path = seckillService.getSeckillPath(10001L, 1L);
        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, path);

        assertFalse(result.isSuccess());
        assertEquals("商品已售罄", result.getMessage());
    }

    @Test
    void testDoSeckill_StockNotInitialized() {
        // 模拟库存未初始化
        when(stockService.deductStock(anyString())).thenReturn(-1L);

        String path = seckillService.getSeckillPath(10001L, 1L);
        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, path);

        assertFalse(result.isSuccess());
        assertEquals("活动未开始或商品不存在", result.getMessage());
    }

    @Test
    void testDoSeckill_RollbackOnException() {
        // 模拟扣库存成功，但数据库插入失败
        when(stockService.deductStock(anyString())).thenReturn(10L);
        when(orderMapper.insert(any(SeckillOrder.class)))
            .thenThrow(new RuntimeException("数据库异常"));

        String path = seckillService.getSeckillPath(10001L, 1L);
        SeckillService.SeckillResult result = seckillService.doSeckill(10001L, 1L, path);

        assertFalse(result.isSuccess());
        assertEquals("下单失败，请重试", result.getMessage());

        // 验证回滚被调用
        verify(stockService).rollbackStock(anyString());
    }
}
