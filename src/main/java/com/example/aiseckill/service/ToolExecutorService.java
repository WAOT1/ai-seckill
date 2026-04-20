package com.example.aiseckill.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 工具执行器
 * 供 Function Calling 调用
 */
@Slf4j
@Service
public class ToolExecutorService {
    
    @Autowired
    private RedisStockService stockService;
    
    @Autowired
    private SeckillService seckillService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 查询商品库存
     */
    public String queryStock(Long goodsId) {
        String stockKey = "seckill:stock:" + goodsId;
        Long stock = stockService.getStock(stockKey);
        
        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goodsId);
        result.put("stock", stock);
        
        if (stock == -1) {
            result.put("status", "not_initialized");
            result.put("message", "活动未开始");
        } else if (stock == 0) {
            result.put("status", "sold_out");
            result.put("message", "已售罄");
        } else {
            result.put("status", "available");
            result.put("message", "有库存");
        }
        
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\":\"序列化失败\"}";
        }
    }
    
    /**
     * 查询商品信息（简化版）
     */
    public String queryGoodsInfo(Long goodsId) {
        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goodsId);
        
        // 简化：根据 goodsId 返回固定商品信息
        // 实际项目中应从数据库查询
        switch (goodsId.intValue()) {
            case 1:
                result.put("name", "限量版AJ1球鞋");
                result.put("price", 1499.00);
                result.put("seckillPrice", 999.00);
                break;
            case 2:
                result.put("name", "茅台飞天53度");
                result.put("price", 2999.00);
                result.put("seckillPrice", 1499.00);
                break;
            case 3:
                result.put("name", "华为Mate 70 Pro");
                result.put("price", 6999.00);
                result.put("seckillPrice", 5999.00);
                break;
            default:
                result.put("name", "未知商品");
                result.put("price", 0);
                result.put("seckillPrice", 0);
        }
        
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\":\"序列化失败\"}";
        }
    }
    
    /**
     * 获取秒杀路径
     */
    public String getSeckillPath(Long userId, Long goodsId) {
        String path = seckillService.getSeckillPath(userId, goodsId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("goodsId", goodsId);
        result.put("path", path);
        result.put("expireTime", "5分钟");
        
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\":\"序列化失败\"}";
        }
    }
}
