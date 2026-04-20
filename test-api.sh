#!/bin/bash

echo "============================================"
echo "AI 智能秒杀助手 - API 测试脚本"
echo "============================================"
echo ""

BASE_URL="http://localhost:8080"

# 1. 健康检查
echo "[1/6] 测试健康检查..."
if curl -s "$BASE_URL/api/ai/test" | grep -q "服务正常"; then
    echo "✅ 服务正常"
else
    echo "❌ 服务未启动"
    exit 1
fi

# 2. 初始化库存
echo "[2/6] 初始化库存..."
if curl -s -X POST "$BASE_URL/api/goods/1/init?stock=10" | grep -q "200"; then
    echo "✅ 库存初始化成功"
else
    echo "❌ 库存初始化失败"
fi

# 3. 查询库存
echo "[3/6] 查询库存..."
if curl -s "$BASE_URL/api/goods/1/stock" | grep -q "stock"; then
    echo "✅ 库存查询成功"
else
    echo "❌ 库存查询失败"
fi

# 4. 获取秒杀路径
echo "[4/6] 获取秒杀路径..."
PATH_VALUE=$(curl -s "$BASE_URL/api/seckill/1/path?userId=10001" | grep -o '"path":"[^"]*"' | cut -d'"' -f4)
echo "✅ 获取路径: $PATH_VALUE"

# 5. 秒杀下单
echo "[5/6] 秒杀下单..."
if curl -s -X POST "$BASE_URL/api/seckill/1/order" -H "Content-Type: application/json" -d "{\"userId\":10001,\"path\":\"$PATH_VALUE\"}" | grep -q "抢购成功"; then
    echo "✅ 秒杀成功"
else
    echo "❌ 秒杀失败"
fi

# 6. 重复下单（测试幂等）
echo "[6/6] 测试幂等性..."
PATH_VALUE2=$(curl -s "$BASE_URL/api/seckill/1/path?userId=10001" | grep -o '"path":"[^"]*"' | cut -d'"' -f4)
if curl -s -X POST "$BASE_URL/api/seckill/1/order" -H "Content-Type: application/json" -d "{\"userId\":10001,\"path\":\"$PATH_VALUE2\"}" | grep -q "已参与"; then
    echo "✅ 幂等性正常（重复下单被拒绝）"
else
    echo "❌ 幂等性测试失败"
fi

echo ""
echo "============================================"
echo "测试完成！去 MySQL 验证数据："
echo "SELECT * FROM seckill_order;"
echo "============================================"
