package com.example.aiseckill.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * DeepSeek Function Calling 响应
 */
@Data
public class DeepSeekFunctionResponse {
    private List<Choice> choices;
    
    @Data
    public static class Choice {
        private Message message;
    }
    
    @Data
    public static class Message {
        private String role;
        private String content;
        private List<ToolCall> tool_calls;
    }
    
    @Data
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;
    }
    
    @Data
    public static class Function {
        private String name;
        private String arguments;
    }
}
