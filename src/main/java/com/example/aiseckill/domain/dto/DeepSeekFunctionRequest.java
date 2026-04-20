package com.example.aiseckill.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek Function Calling 请求
 */
@Data
public class DeepSeekFunctionRequest {
    private String model = "deepseek-chat";
    private List<Message> messages;
    private List<Tool> tools;
    private Double temperature = 0.3;
    
    @Data
    public static class Message {
        private String role;
        private String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
    
    @Data
    public static class Tool {
        private String type = "function";
        private Function function;
        
        public Tool(Function function) {
            this.function = function;
        }
    }
    
    @Data
    public static class Function {
        private String name;
        private String description;
        private Parameters parameters;
    }
    
    @Data
    public static class Parameters {
        private String type = "object";
        private Map<String, Property> properties;
        private List<String> required;
    }
    
    @Data
    public static class Property {
        private String type;
        private String description;
    }
}
