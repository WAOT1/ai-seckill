package com.example.aiseckill.service;

import com.example.aiseckill.domain.dto.DeepSeekFunctionRequest;
import com.example.aiseckill.domain.dto.DeepSeekFunctionResponse;
import com.example.aiseckill.gateway.LLMGateway;
import com.example.aiseckill.gateway.LLMGatewayException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ReAct Agent 服务
 * 实现 Thought → Action → Observation 循环架构
 */
@Slf4j
@Service
public class ReActAgentService {

    @Autowired
    private LLMGateway llmGateway;

    @Autowired
    private ToolExecutorService toolExecutor;

    @Autowired
    private List<DeepSeekFunctionRequest.Tool> seckillTools;

    @Autowired
    private AgentMemoryService agentMemory;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * ReAct Agent 执行入口
     * @param userQuery 用户输入
     * @param userId 用户ID（用于记忆）
     * @return Agent执行结果
     */
    public AgentResult execute(String userQuery, Long userId) {
        log.info("ReAct Agent开始执行，用户查询: {}", userQuery);
        
        // 获取用户记忆（偏好、历史）
        String memory = agentMemory.getUserMemory(userId);
        
        // 构建系统Prompt（ReAct格式）
        String systemPrompt = buildReActSystemPrompt(memory);
        
        // 初始化消息列表
        List<DeepSeekFunctionRequest.Message> messages = new ArrayList<>();
        messages.add(new DeepSeekFunctionRequest.Message("system", systemPrompt));
        messages.add(new DeepSeekFunctionRequest.Message("user", userQuery));
        
        // ReAct循环
        int maxIterations = 5;
        StringBuilder thoughtProcess = new StringBuilder();
        
        for (int i = 0; i < maxIterations; i++) {
            log.info("ReAct迭代 {}/{}: 调用LLM思考...", i + 1, maxIterations);
            
            // 1. Thought: AI思考下一步
            DeepSeekFunctionResponse response = callWithTools(messages);
            
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                return AgentResult.error("AI服务调用失败");
            }
            
            DeepSeekFunctionResponse.Message aiMessage = response.getChoices().get(0).getMessage();
            String content = aiMessage.getContent();
            
            // 记录思考过程
            thoughtProcess.append("[思考] ").append(content).append("\n");
            
            // 2. 检查是否需要调用工具
            if (aiMessage.getTool_calls() != null && !aiMessage.getTool_calls().isEmpty()) {
                // 需要执行Action
                DeepSeekFunctionResponse.ToolCall toolCall = aiMessage.getTool_calls().get(0);
                String functionName = toolCall.getFunction().getName();
                String arguments = toolCall.getFunction().getArguments();
                
                log.info("AI决定调用工具: {}, 参数: {}", functionName, arguments);
                thoughtProcess.append("[行动] 调用 ").append(functionName).append("(").append(arguments).append(")\n");
                
                // 3. Action: 执行工具
                String toolResult = executeTool(functionName, arguments);
                log.info("工具执行结果: {}", toolResult);
                thoughtProcess.append("[观察] ").append(toolResult).append("\n");
                
                // 将结果加入对话上下文
                messages.add(new DeepSeekFunctionRequest.Message("assistant", content));
                messages.add(new DeepSeekFunctionRequest.Message("tool", toolResult));
                
            } else {
                // 不需要调用工具，生成最终回答
                log.info("AI生成最终回答");
                thoughtProcess.append("[完成] ").append(content).append("\n");
                
                // 更新用户记忆
                agentMemory.updateMemory(userId, userQuery, content);
                
                return AgentResult.success(content, thoughtProcess.toString());
            }
        }
        
        // 超过最大迭代次数
        return AgentResult.error("任务过于复杂，Agent无法在规定时间内完成");
    }
    
    /**
     * 构建ReAct系统Prompt
     */
    private String buildReActSystemPrompt(String memory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是智能电商Agent，具备自主决策和工具调用能力。\n\n");
        
        if (memory != null && !memory.isEmpty()) {
            prompt.append("用户偏好:\n").append(memory).append("\n\n");
        }
        
        prompt.append("执行规则:\n");
        prompt.append("1. 分析用户需求，确定需要调用的工具\n");
        prompt.append("2. 每次只能调用一个工具\n");
        prompt.append("3. 根据工具返回结果，决定下一步行动\n");
        prompt.append("4. 当获取足够信息后，生成最终回答\n");
        prompt.append("5. 如果无法完成任务，说明原因\n\n");
        prompt.append("可用工具:\n");
        prompt.append("- query_stock: 查询商品库存\n");
        prompt.append("- query_goods_info: 查询商品信息\n");
        prompt.append("- get_seckill_path: 获取秒杀路径\n");
        prompt.append("- query_order: 查询订单状态\n");
        prompt.append("- query_logistics: 查询物流信息\n");
        prompt.append("- create_order: 创建订单\n\n");
        prompt.append("思考格式:\n");
        prompt.append("Thought: 我需要... → Action: 调用工具 → Observation: 结果...");
        
        return prompt.toString();
    }
    
    /**
     * 调用DeepSeek API（带工具）
     */
    private DeepSeekFunctionResponse callWithTools(List<DeepSeekFunctionRequest.Message> messages) {
        try {
            return llmGateway.chatWithTools(messages, seckillTools);
        } catch (LLMGatewayException e) {
            log.error("ReAct Agent调用LLM失败", e);
            return null;
        }
    }
    
    /**
     * 执行工具
     */
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
                    
                case "query_order":
                    Long orderNo = args.get("orderNo").asLong();
                    return toolExecutor.queryOrder(orderNo);
                    
                case "query_logistics":
                    Long logisticsOrderNo = args.get("orderNo").asLong();
                    return toolExecutor.queryLogistics(logisticsOrderNo);
                    
                case "create_order":
                    Long createUserId = args.get("userId").asLong();
                    Long createGoodsId = args.get("goodsId").asLong();
                    return toolExecutor.createOrder(createUserId, createGoodsId);
                    
                default:
                    return "{\"error\":\"未知函数: " + functionName + "\"}";
            }
        } catch (Exception e) {
            log.error("ReAct Agent执行工具失败", e);
            return "{\"error\":\"执行失败: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * Agent执行结果
     */
    public static class AgentResult {
        private boolean success;
        private String answer;
        private String thoughtProcess;
        private String error;
        
        public static AgentResult success(String answer, String thoughtProcess) {
            AgentResult result = new AgentResult();
            result.success = true;
            result.answer = answer;
            result.thoughtProcess = thoughtProcess;
            return result;
        }
        
        public static AgentResult error(String error) {
            AgentResult result = new AgentResult();
            result.success = false;
            result.error = error;
            return result;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getAnswer() { return answer; }
        public String getThoughtProcess() { return thoughtProcess; }
        public String getError() { return error; }
    }
}