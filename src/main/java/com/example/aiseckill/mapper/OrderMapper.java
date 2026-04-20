package com.example.aiseckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiseckill.domain.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<SeckillOrder> {
    // BaseMapper 已提供 insert, selectById, selectList 等方法
}
