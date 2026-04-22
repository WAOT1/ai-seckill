package com.example.aiseckill.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 记忆服务
 * 存储用户偏好、历史行为，实现个性化服务
 */
@Slf4j
@Service
public class AgentMemoryService {
    
    /**
     * 用户记忆存储（简化版，生产环境应使用数据库）
     * Key: userId, Value: 用户记忆文本
     */
    private final Map<Long, String> userMemoryStore = new ConcurrentHashMap<>();
    
    /**
     * 获取用户记忆
     * @param userId 用户ID
     * @return 用户记忆文本
     */
    public String getUserMemory(Long userId) {
        if (userId == null) {
            return "";
        }
        
        String memory = userMemoryStore.get(userId);
        if (memory == null) {
            // 新用户，初始化默认记忆
            memory = "新用户，暂无偏好记录";
            userMemoryStore.put(userId, memory);
        }
        
        log.debug("获取用户{}记忆: {}", userId, memory);
        return memory;
    }
    
    /**
     * 更新用户记忆
     * @param userId 用户ID
     * @param query 用户查询
     * @param response Agent回答
     */
    public void updateMemory(Long userId, String query, String response) {
        if (userId == null) {
            return;
        }
        
        // 分析用户偏好（简化版）
        StringBuilder memory = new StringBuilder();
        
        // 提取商品偏好（使用通用示例，避免商标侵权）
        if (query.contains("球鞋") || query.contains("运动鞋")) {
            memory.append("偏好品类: 运动鞋\n");
        } else if (query.contains("白酒") || query.contains("酒")) {
            memory.append("偏好品类: 酒类\n");
        } else if (query.contains("手机") || query.contains("电子产品")) {
            memory.append("偏好品类: 电子产品\n");
        }
        
        // 提取价格敏感度
        if (query.contains("便宜") || query.contains("优惠") || query.contains("折扣")) {
            memory.append("价格敏感: 是\n");
        }
        
        // 提取购买意向
        if (query.contains("买") || query.contains("下单") || query.contains("抢购")) {
            memory.append("购买意向: 高\n");
        }
        
        // 合并旧记忆（如果有）
        String oldMemory = userMemoryStore.get(userId);
        if (oldMemory != null && !oldMemory.equals("新用户，暂无偏好记录")) {
            memory.append("历史记录:\n").append(oldMemory);
        }
        
        String finalMemory = memory.toString();
        if (!finalMemory.isEmpty()) {
            userMemoryStore.put(userId, finalMemory);
            log.info("更新用户{}记忆: {}", userId, finalMemory);
        }
    }
    
    /**
     * 清除用户记忆
     * @param userId 用户ID
     */
    public void clearMemory(Long userId) {
        if (userId != null) {
            userMemoryStore.remove(userId);
            log.info("清除用户{}记忆", userId);
        }
    }
    
    /**
     * 获取用户画像（结构化）
     * @param userId 用户ID
     * @return 用户画像Map
     */
    public Map<String, String> getUserProfile(Long userId) {
        String memory = getUserMemory(userId);
        Map<String, String> profile = new ConcurrentHashMap<>();
        
        // 解析记忆文本（简化版）
        String[] lines = memory.split("\n");
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    profile.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        
        return profile;
    }
}