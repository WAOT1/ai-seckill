package com.example.aiseckill.controller;

import com.example.aiseckill.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIPredictController {
    
    private final AIService aiService;
    
    /**
     * AI 预测下次补货时间
     */
    @GetMapping("/predict/{goodsId}")
    public Map<String, Object> predict(@PathVariable Long goodsId) {
        String result = aiService.predictRestock(goodsId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", Map.of(
            "goodsId", goodsId,
            "prediction", result
        ));
        return response;
    }
    
    /**
     * AI 风控检查
     */
    @PostMapping("/risk-check")
    public Map<String, Object> riskCheck(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String result = aiService.checkRisk(userId, params);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", Map.of(
            "userId", userId,
            "riskAnalysis", result
        ));
        return response;
    }
}
