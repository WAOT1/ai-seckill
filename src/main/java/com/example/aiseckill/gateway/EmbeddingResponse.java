package com.example.aiseckill.gateway;

import lombok.Data;
import java.util.List;

/**
 * 文本嵌入响应
 */
@Data
public class EmbeddingResponse {

    private List<Double> embedding;
    private String model;
    private Integer dimension;
}
