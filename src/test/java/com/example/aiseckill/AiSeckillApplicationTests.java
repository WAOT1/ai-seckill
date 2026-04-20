package com.example.aiseckill;

import com.example.aiseckill.service.RedisStockService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * 上下文加载测试
 * 使用 WebEnvironment.NONE 避免启动 Web 服务器
 * Mock RedisStockService 避免 Redis 连接检查导致启动失败
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AiSeckillApplicationTests {

    @MockBean
    private RedisStockService redisStockService;

    @Test
    void contextLoads() {
        // 验证 Spring 上下文能正常加载
    }

}
