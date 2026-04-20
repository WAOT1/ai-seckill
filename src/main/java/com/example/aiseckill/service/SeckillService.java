package com.example.aiseckill.service;

import com.example.aiseckill.domain.entity.SeckillOrder;
import com.example.aiseckill.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务
 * 生产环境依赖 Redis 高可用，不提供内存降级
 */
@Slf4j
@Service
public class SeckillService {
    
    @Autowired
    private RedisStockService stockService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private OrderMapper orderMapper;
    
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USER_KEY_PREFIX = "seckill:user:";
    private static final String PATH_KEY_PREFIX = "seckill:path:";
    
    public String getSeckillPath(Long userId, Long goodsId) {
        String path = generatePath();
        String pathKey = PATH_KEY_PREFIX + userId + ":" + goodsId;
        redisTemplate.opsForValue().set(pathKey, path, 5, TimeUnit.MINUTES);
        return path;
    }
    
    public SeckillResult doSeckill(Long userId, Long goodsId, String path) {
        String pathKey = PATH_KEY_PREFIX + userId + ":" + goodsId;
        String expectedPath = redisTemplate.opsForValue().get(pathKey);
        if (expectedPath == null || !expectedPath.equals(path)) {
            return SeckillResult.fail("非法请求或链接已过期");
        }
        
        String userKey = USER_KEY_PREFIX + userId + ":" + goodsId;
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(userKey, "1", 10, TimeUnit.MINUTES);
        boolean hasBought = Boolean.FALSE.equals(result);
        
        if (hasBought) {
            return SeckillResult.fail("您已参与过该活动");
        }
        
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        Long stock = stockService.deductStock(stockKey);
        
        if (stock == null || stock == -1) {
            clearUserFlag(userKey);
            return SeckillResult.fail("活动未开始或商品不存在");
        }
        if (stock == 0) {
            clearUserFlag(userKey);
            return SeckillResult.fail("商品已售罄");
        }
        
        try {
            SeckillOrder order = createOrder(userId, goodsId);
            clearPath(pathKey);
            log.info("用户{}抢购成功，订单号：{}，剩余：{}", userId, order.getOrderNo(), stock - 1);
            return SeckillResult.success(order);
        } catch (Exception e) {
            stockService.rollbackStock(stockKey);
            clearUserFlag(userKey);
            log.error("下单失败", e);
            return SeckillResult.fail("下单失败，请重试");
        }
    }
    
    private void clearUserFlag(String userKey) {
        redisTemplate.delete(userKey);
    }
    
    private void clearPath(String pathKey) {
        redisTemplate.delete(pathKey);
    }
    
    private SeckillOrder createOrder(Long userId, Long goodsId) {
        SeckillOrder order = new SeckillOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setQuantity(1);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }
    
    private String generateOrderNo() {
        return "SK" + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
            + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    private String generatePath() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    public static class SeckillResult {
        private boolean success;
        private String message;
        private SeckillOrder order;
        
        public static SeckillResult success(SeckillOrder order) {
            SeckillResult r = new SeckillResult();
            r.success = true; r.order = order; r.message = "抢购成功";
            return r;
        }
        public static SeckillResult fail(String message) {
            SeckillResult r = new SeckillResult();
            r.success = false; r.message = message;
            return r;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public SeckillOrder getOrder() { return order; }
    }
}
