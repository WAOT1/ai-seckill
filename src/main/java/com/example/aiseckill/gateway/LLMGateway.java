package com.example.aiseckill.gateway;

import com.example.aiseckill.domain.dto.DeepSeekFunctionRequest;
import com.example.aiseckill.domain.dto.DeepSeekFunctionResponse;
import java.util.List;

/**
 * LLM 网关接口（防腐层）
 * 隔离外部 LLM API 的具体实现，防止外部领域模型腐蚀内部业务逻辑
 */
public interface LLMGateway {

    /**
     * 普通对话
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户输入
     * @return AI 回复内容
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 带 Function Calling 的对话
     *
     * @param messages 消息列表
     * @param tools    可用工具定义
     * @return AI 响应（包含可能的工具调用请求）
     */
    DeepSeekFunctionResponse chatWithTools(List<DeepSeekFunctionRequest.Message> messages,
                                            List<DeepSeekFunctionRequest.Tool> tools);

    /**
     * 文本嵌入（向量化）
     *
     * @param text 待嵌入文本
     * @return 嵌入向量响应
     */
    EmbeddingResponse embed(String text);
}
