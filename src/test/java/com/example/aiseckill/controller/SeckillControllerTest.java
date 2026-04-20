package com.example.aiseckill.controller;

import com.example.aiseckill.service.AIService;
import com.example.aiseckill.service.RedisStockService;
import com.example.aiseckill.service.SeckillService;
import com.example.aiseckill.domain.entity.SeckillOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SeckillController 单元测试
 * 使用 @SpringBootTest + @AutoConfigureMockMvc 避免 ApplicationContext 加载问题
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeckillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeckillService seckillService;

    @MockBean
    private RedisStockService redisStockService;

    @MockBean
    private AIService aiService;

    @Test
    void testGetPath() throws Exception {
        when(seckillService.getSeckillPath(10001L, 1L)).thenReturn("abc123");

        mockMvc.perform(get("/api/seckill/1/path")
                .param("userId", "10001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.path").value("abc123"));
    }

    @Test
    void testOrder_Success() throws Exception {
        SeckillService.SeckillResult result = SeckillService.SeckillResult.success(new SeckillOrder() {{
            setOrderNo("SK20250419000001");
            setStatus(0);
        }});

        when(seckillService.doSeckill(10002L, 1L, "abc123")).thenReturn(result);

        mockMvc.perform(post("/api/seckill/1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":10002,\"path\":\"abc123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("抢购成功"));
    }

    @Test
    void testOrder_Duplicate() throws Exception {
        SeckillService.SeckillResult result = SeckillService.SeckillResult.fail("您已参与过该活动");
        when(seckillService.doSeckill(10003L, 1L, "abc123")).thenReturn(result);

        mockMvc.perform(post("/api/seckill/1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":10003,\"path\":\"abc123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("您已参与过该活动"));
    }

    @Test
    void testOrder_InvalidPath() throws Exception {
        SeckillService.SeckillResult result = SeckillService.SeckillResult.fail("非法请求或链接已过期");
        when(seckillService.doSeckill(10004L, 1L, "invalid")).thenReturn(result);

        mockMvc.perform(post("/api/seckill/1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":10004,\"path\":\"invalid\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("非法请求或链接已过期"));
    }
}
