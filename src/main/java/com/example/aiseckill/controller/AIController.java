package com.example.aiseckill.controller;

import com.example.aiseckill.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    
    /**
     * 普通 AI 对话
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam String q) {
        String answer = aiService.chat(q);
        return Map.of(
            "code", 200,
            "message", "success",
            "data", Map.of("answer", answer)
        );
    }
    
    /**
     * 智能客服（带 Function Calling）
     * AI 可以调用系统函数查库存、查商品信息
     */
    @GetMapping("/assistant")
    public Map<String, Object> assistant(@RequestParam String q) {
        String answer = aiService.chatWithTools(q);
        return Map.of(
            "code", 200,
            "message", "success",
            "data", Map.of("answer", answer)
        );
    }
    
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", 200);
        result.put("message", "服务正常");
        result.put("data", null);
        return result;
    }
}
