package com.example.aiseckill.gateway;

import com.example.aiseckill.domain.dto.DeepSeekFunctionRequest;
import com.example.aiseckill.domain.dto.DeepSeekFunctionResponse;
import com.example.aiseckill.domain.dto.DeepSeekRequest;
import com.example.aiseckill.domain.dto.DeepSeekResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * DeepSeek API 网关实现
 * 封装 DeepSeek 的 HTTP 调用细节，处理请求/响应转换和异常处理
 */
@Slf4j
@Component
public class DeepSeekGateway implements LLMGateway {

    @Value("${deepseek.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public DeepSeekGateway() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.deepseek.com")
            .build();
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
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
            return null;
        } catch (WebClientResponseException e) {
            log.error("DeepSeek API 返回错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new LLMGatewayException("LLM 服务返回错误: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("调用 DeepSeek chat 接口失败", e);
            throw new LLMGatewayException("LLM 服务调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DeepSeekFunctionResponse chatWithTools(List<DeepSeekFunctionRequest.Message> messages,
                                                   List<DeepSeekFunctionRequest.Tool> tools) {
        try {
            DeepSeekFunctionRequest request = new DeepSeekFunctionRequest();
            request.setMessages(messages);
            request.setTools(tools);
            request.setTemperature(0.3);

            return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DeepSeekFunctionResponse.class)
                .block();
        } catch (WebClientResponseException e) {
            log.error("DeepSeek Function Calling API 返回错误: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new LLMGatewayException("LLM 工具调用服务返回错误: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("调用 DeepSeek Function Calling 接口失败", e);
            throw new LLMGatewayException("LLM 工具调用服务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public EmbeddingResponse embed(String text) {
        log.warn("DeepSeek embed 功能尚未实现");
        EmbeddingResponse response = new EmbeddingResponse();
        response.setEmbedding(List.of());
        response.setModel("deepseek-embedding");
        response.setDimension(0);
        return response;
    }
}
