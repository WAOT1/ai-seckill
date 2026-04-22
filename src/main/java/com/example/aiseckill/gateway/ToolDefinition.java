package com.example.aiseckill.gateway;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 工具定义（防腐层通用模型）
 * 用于描述 LLM 可调用的外部工具
 */
@Data
public class ToolDefinition {

    private String name;
    private String description;
    private Map<String, Property> parameters;
    private List<String> required;

    @Data
    public static class Property {
        private String type;
        private String description;
    }
}
