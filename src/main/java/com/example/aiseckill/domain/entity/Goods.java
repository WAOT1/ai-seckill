package com.example.aiseckill.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Goods {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private String imgUrl;
    private Integer status; // 0-下架 1-上架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
