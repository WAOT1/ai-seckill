package com.example.aiseckill.service;

import com.example.aiseckill.domain.dto.DeepSeekFunctionRequest;
import com.example.aiseckill.domain.dto.DeepSeekFunctionResponse;
import com.example.aiseckill.domain.dto.DeepSeekRequest;
import com.example.aiseckill.domain.dto.DeepSeekResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIService {
    
    @Value("${deepseek.api-key}")
    private String apiKey;
    
    @Autowired
    private ToolExecutorService toolExecutor;
    
    @Autowired
    private List<DeepSeekFunctionRequest.Tool> seckillTools;
    
    private final WebClient webClient = WebClient.builder()
        .baseUrl("https://api.deepseek.com")
        .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 普通对话（不带 Function Calling）
     */
    public String chat(String question) {
        return callDeepSeek("你是一个专业的限量商品抢购助手", question);
    }
    
    /**
     * 智能客服（带 Function Calling）
     * AI 可以调用系统函数获取真实数据
     */
    public String chatWithTools(String question) {
        // 第 1 步：发送请求，带上可用函数
        List<DeepSeekFunctionRequest.Message> messages = new ArrayList<>();
        messages.add(new DeepSeekFunctionRequest.Message("system", 
            "你是智能秒杀助手。当用户询问库存、商品信息时，你必须调用提供的函数获取真实数据，不要编造。"));
        messages.add(new DeepSeekFunctionRequest.Message("user", question));
        
        DeepSeekFunctionResponse response = callWithTools(messages);
        
        // 第 2 步：检查 AI 是否要求调用函数
        if (response != null 
            && response.getChoices() != null 
            && !response.getChoices().isEmpty()
            && response.getChoices().get(0).getMessage().getTool_calls() != null) {
            
            // AI 要求调用函数
            DeepSeekFunctionResponse.ToolCall toolCall = 
                response.getChoices().get(0).getMessage().getTool_calls().get(0);
            
            String functionName = toolCall.getFunction().getName();
            String arguments = toolCall.getFunction().getArguments();
            
            log.info("AI 要求调用函数: {}, 参数: {}", functionName, arguments);
            
            // 执行函数
            String toolResult = executeTool(functionName, arguments);
            
            // 第 3 步：把函数结果再发给 AI
            // 注意：需要把 Response 的 Message 转为 Request 的 Message
            DeepSeekFunctionResponse.Message aiMessage = response.getChoices().get(0).getMessage();
            messages.add(new DeepSeekFunctionRequest.Message("assistant", aiMessage.getContent()));
            messages.add(new DeepSeekFunctionRequest.Message("tool", toolResult));
            
            DeepSeekFunctionResponse finalResponse = callWithTools(messages);
            
            if (finalResponse != null && !finalResponse.getChoices().isEmpty()) {
                return finalResponse.getChoices().get(0).getMessage().getContent();
            }
        }
        
        // AI 直接回答，不需要调用函数
        if (response != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        
        return "AI暂时无法回答";
    }
    
    /**
     * AI 预测补货时间
     */
    public String predictRestock(Long goodsId) {
        String prompt = buildPredictPrompt(goodsId);
        return callDeepSeek("你是限量商品库存预测专家", prompt);
    }
    
    /**
     * AI 风控检查
     */
    public String checkRisk(Long userId, Map<String, Object> behavior) {
        String prompt = buildRiskPrompt(userId, behavior);
        return callDeepSeek("你是电商反欺诈专家", prompt);
    }
    
    private DeepSeekFunctionResponse callWithTools(List<DeepSeekFunctionRequest.Message> messages) {
        try {
            DeepSeekFunctionRequest request = new DeepSeekFunctionRequest();
            request.setMessages(messages);
            request.setTools(seckillTools);
            
            return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DeepSeekFunctionResponse.class)
                .block();
        } catch (Exception e) {
            log.error("Function Calling 调用失败", e);
            return null;
        }
    }
    
    private String executeTool(String functionName, String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            
            switch (functionName) {
                case "query_stock":
                    Long goodsId = args.get("goodsId").asLong();
                    return toolExecutor.queryStock(goodsId);
                    
                case "query_goods_info":
                    Long gid = args.get("goodsId").asLong();
                    return toolExecutor.queryGoodsInfo(gid);
                    
                case "get_seckill_path":
                    Long userId = args.get("userId").asLong();
                    Long gId = args.get("goodsId").asLong();
                    return toolExecutor.getSeckillPath(userId, gId);
                    
                default:
                    return "未知函数: " + functionName;
            }
        } catch (Exception e) {
            log.error("执行工具失败", e);
            return "执行失败: " + e.getMessage();
        }
    }
    
    private String callDeepSeek(String systemPrompt, String userPrompt) {
        DeepSeekRequest request = new DeepSeekRequest();
        request.setMessages(List.of(
            new DeepSeekRequest.Message("system", systemPrompt),
            new DeepSeekRequest.Message("user", userPrompt)
        ));
        
        try {
            DeepSeekResponse response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DeepSeekResponse.class)
                .block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return "AI暂时无法回答";
        } catch (Exception e) {
            log.error("调用DeepSeek失败", e);
            return "服务暂时不可用: " + e.getMessage();
        }
    }
    
    private String buildPredictPrompt(Long goodsId) {
        return String.format("""
            商品ID: %d
            历史数据: 该商品过去30天内售罄3次，平均补货周期5-7天。
            
            请预测下次补货时间，并给出置信度和理由。
            以JSON格式输出: {"predictedTime": "2025-04-25T10:00:00", "confidence": 0.85, "reason": "..."}
            """, goodsId);
    }
    
    private String buildRiskPrompt(Long userId, Map<String, Object> behavior) {
        return String.format("""
            用户ID: %d
            行为数据: %s
            
            请分析该用户是否存在脚本抢购或黄牛行为。
            以JSON格式输出: {"riskScore": 75, "riskLevel": "high", "isBlocked": true, "reason": "..."}
            """, userId, behavior.toString());
    }
}
