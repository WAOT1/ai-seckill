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
    
    /**
     * 查询订单状态
     */
    public String queryOrder(Long orderNo) {
        // 模拟订单查询（实际应从数据库查询）
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        
        // 模拟不同订单状态
        if (orderNo % 3 == 0) {
            result.put("status", "已支付");
            result.put("statusCode", 1);
            result.put("message", "订单已支付，等待发货");
        } else if (orderNo % 3 == 1) {
            result.put("status", "已发货");
            result.put("statusCode", 2);
            result.put("message", "订单已发货，物流运输中");
        } else {
            result.put("status", "已完成");
            result.put("statusCode", 3);
            result.put("message", "订单已完成，感谢购买");
        }
        
        result.put("createTime", "2026-04-20 10:30:00");
        
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\":\"序列化失败\"}";
        }
    }
    
    /**
     * 查询物流信息
     */
    public String queryLogistics(Long orderNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("carrier", "顺丰速运");
        result.put("trackingNo", "SF" + orderNo + "8888");
        
        // 模拟物流状态
        if (orderNo % 4 == 0) {
            result.put("status", "已签收");
            result.put("location", "北京市朝阳区");
        } else if (orderNo % 4 == 1) {
            result.put("status", "运输中");
            result.put("location", "上海市浦东新区");
        } else if (orderNo % 4 == 2) {
            result.put("status", "已揽收");
            result.put("location", "深圳市南山区");
        } else {
            result.put("status", "派送中");
            result.put("location", "广州市天河区");
        }
        
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\":\"序列化失败\"}";
        }
    }
    
    /**
     * 创建订单
     */
    public String createOrder(Long userId, Long goodsId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查库存
            String stockKey = "seckill:stock:" + goodsId;
            Long stock = stockService.getStock(stockKey);
            
            if (stock == null || stock <= 0) {
                result.put("success", false);
                result.put("message", "商品库存不足");
                return objectMapper.writeValueAsString(result);
            }
            
            // 模拟创建订单
            Long orderNo = System.currentTimeMillis();
            result.put("success", true);
            result.put("orderNo", orderNo);
            result.put("userId", userId);
            result.put("goodsId", goodsId);
            result.put("status", "已创建");
            result.put("message", "订单创建成功，请在30分钟内支付");
            
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\":\"创建订单失败: " + e.getMessage() + "\"}";
        }
    }
}
