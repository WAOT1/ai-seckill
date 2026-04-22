package com.example.aiseckill.config;

import com.example.aiseckill.domain.dto.DeepSeekFunctionRequest.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * AI 可调用的函数定义（Function Calling Schema）
 */
@Configuration
public class FunctionSchemaConfig {
    
    @Bean
    public List<Tool> seckillTools() {
        return List.of(
            // 1. 查询库存
            new Tool(createFunction(
                "query_stock",
                "查询商品实时库存",
                Map.of(
                    "goodsId", createProperty("integer", "商品ID")
                ),
                List.of("goodsId")
            )),
            
            // 2. 查询商品信息
            new Tool(createFunction(
                "query_goods_info",
                "查询商品详细信息（名称、价格、图片等）",
                Map.of(
                    "goodsId", createProperty("integer", "商品ID")
                ),
                List.of("goodsId")
            )),
            
            // 3. 获取秒杀路径
            new Tool(createFunction(
                "get_seckill_path",
                "为用户生成秒杀抢购路径",
                Map.of(
                    "userId", createProperty("integer", "用户ID"),
                    "goodsId", createProperty("integer", "商品ID")
                ),
                List.of("userId", "goodsId")
            )),
            
            // 4. 查询订单
            new Tool(createFunction(
                "query_order",
                "查询订单状态和详情",
                Map.of(
                    "orderNo", createProperty("integer", "订单号")
                ),
                List.of("orderNo")
            )),
            
            // 5. 查询物流
            new Tool(createFunction(
                "query_logistics",
                "查询订单物流信息",
                Map.of(
                    "orderNo", createProperty("integer", "订单号")
                ),
                List.of("orderNo")
            )),
            
            // 6. 创建订单
            new Tool(createFunction(
                "create_order",
                "创建新订单",
                Map.of(
                    "userId", createProperty("integer", "用户ID"),
                    "goodsId", createProperty("integer", "商品ID")
                ),
                List.of("userId", "goodsId")
            ))
        );
    }
    
    private Function createFunction(String name, String description, 
                                     Map<String, Property> properties, 
                                     List<String> required) {
        Function func = new Function();
        func.setName(name);
        func.setDescription(description);
        
        Parameters params = new Parameters();
        params.setType("object");
        params.setProperties(properties);
        params.setRequired(required);
        func.setParameters(params);
        
        return func;
    }
    
    private Property createProperty(String type, String description) {
        Property prop = new Property();
        prop.setType(type);
        prop.setDescription(description);
        return prop;
    }
}
