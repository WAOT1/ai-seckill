package com.example.aiseckill.gateway;

/**
 * LLM 网关异常
 * 将外部 LLM API 异常转换为业务领域异常
 */
public class LLMGatewayException extends RuntimeException {

    public LLMGatewayException(String message) {
        super(message);
    }

    public LLMGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
