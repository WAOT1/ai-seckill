package com.example.aiseckill.controller;

import com.example.aiseckill.service.SeckillService;
import com.example.aiseckill.service.SeckillService.SeckillResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {
    
    private final SeckillService seckillService;
    
    @GetMapping("/{goodsId}/path")
    public Map<String, Object> getPath(
            @PathVariable Long goodsId,
            @RequestParam Long userId) {
        String path = seckillService.getSeckillPath(userId, goodsId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
            "path", path,
            "expireTime", "5分钟"
        ));
        return result;
    }
    
    @PostMapping("/{goodsId}/order")
    public Map<String, Object> order(
            @PathVariable Long goodsId,
            @RequestBody Map<String, Object> params) {
        
        Long userId = Long.valueOf(params.get("userId").toString());
        String path = (String) params.get("path");
        
        SeckillResult result = seckillService.doSeckill(userId, goodsId, path);
        
        Map<String, Object> response = new HashMap<>();
        if (result.isSuccess()) {
            response.put("code", 200);
            response.put("message", result.getMessage());
            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", result.getOrder().getOrderNo());
            data.put("status", result.getOrder().getStatus());
            data.put("createTime", result.getOrder().getCreateTime());
            response.put("data", data);
        } else {
            response.put("code", 400);
            response.put("message", result.getMessage());
            response.put("data", null);
        }
        return response;
    }
}
