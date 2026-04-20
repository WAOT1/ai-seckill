package com.example.aiseckill.controller;

import com.example.aiseckill.service.AIService;
import com.example.aiseckill.service.RedisStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {
    
    private final RedisStockService stockService;
    
    @PostMapping("/{goodsId}/init")
    public Map<String, Object> initStock(
            @PathVariable Long goodsId,
            @RequestParam int stock) {
        String stockKey = "seckill:stock:" + goodsId;
        stockService.initStock(stockKey, stock);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "库存初始化成功");
        result.put("data", Map.of("goodsId", goodsId, "stock", stock));
        return result;
    }
    
    @GetMapping("/{goodsId}/stock")
    public Map<String, Object> getStock(@PathVariable Long goodsId) {
        String stockKey = "seckill:stock:" + goodsId;
        Long stock = stockService.getStock(stockKey);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("goodsId", goodsId, "stock", stock));
        return result;
    }
}
