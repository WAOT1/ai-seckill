package com.example.aiseckill.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeepSeekResponse {
    private List<Choice> choices;
    
    @Data
    public static class Choice {
        private Message message;
    }
    
    @Data
    public static class Message {
        private String content;
    }
}
