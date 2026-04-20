package com.example.aiseckill.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeepSeekRequest {
    private String model = "deepseek-chat";
    private List<Message> messages;
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
}
