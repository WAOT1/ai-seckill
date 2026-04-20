package com.example.aiseckill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 上下文加载测试
 * 使用 WebEnvironment.NONE 避免启动 Web 服务器
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AiSeckillApplicationTests {

    @Test
    void contextLoads() {
        // 验证 Spring 上下文能正常加载
    }

}
