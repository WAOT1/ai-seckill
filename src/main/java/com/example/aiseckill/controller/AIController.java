package com.example.aiseckill.controller;

import com.example.aiseckill.common.exception.BusinessException;
import com.example.aiseckill.common.response.ApiResponse;
import com.example.aiseckill.service.AIService;
import com.example.aiseckill.service.ReActAgentService;
import com.example.aiseckill.service.ReActAgentService.AgentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    private final ReActAgentService agentService;
    
    /**
     * 普通 AI 对话
     */
    @GetMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestParam String q) {
        String answer = aiService.chat(q);
        return ApiResponse.success(Map.of("answer", answer));
    }
    
    /**
     * 智能客服（带 Function Calling）
     * AI 可以调用系统函数查库存、查商品信息
     */
    @GetMapping("/assistant")
    public ApiResponse<Map<String, Object>> assistant(@RequestParam String q) {
        String answer = aiService.chatWithTools(q);
        return ApiResponse.success(Map.of("answer", answer));
    }
    
    /**
     * ReAct Agent 执行接口
     * AI Agent自主决策、调用工具完成任务
     */
    @PostMapping("/agent")
    public ApiResponse<Map<String, Object>> agent(@RequestBody Map<String, Object> params) {
        String query = (String) params.get("query");
        Long userId = Long.valueOf(params.getOrDefault("userId", "0").toString());
        
        AgentResult result = agentService.execute(query, userId);
        
        if (!result.isSuccess()) {
            throw new BusinessException(ApiResponse.AI_SERVICE_ERROR, result.getError());
        }
        
        return ApiResponse.success(Map.of(
            "answer", result.getAnswer(),
            "thoughtProcess", result.getThoughtProcess()
        ));
    }
    
    @GetMapping("/test")
    public ApiResponse<Void> test() {
        return ApiResponse.success("服务正常", null);
    }
}
