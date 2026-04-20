# AI 智能秒杀助手 - API 测试脚本 (PowerShell)

$BASE_URL = "http://localhost:8080"

Write-Host "============================================"
Write-Host "AI 智能秒杀助手 - API 测试脚本"
Write-Host "============================================"
Write-Host ""

# 1. 健康检查
Write-Host "[1/6] 测试健康检查..."
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/ai/test" -TimeoutSec 5
    if ($response.code -eq 200) {
        Write-Host "✅ 服务正常"
    } else {
        Write-Host "❌ 服务异常"
        exit
    }
} catch {
    Write-Host "❌ 服务未启动或无法连接"
    Write-Host "错误: $($_.Exception.Message)"
    exit
}

# 2. 初始化库存
Write-Host "[2/6] 初始化库存..."
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/goods/1/init?stock=10" -Method POST -TimeoutSec 5
    if ($response.code -eq 200) {
        Write-Host "✅ 库存初始化成功"
    } else {
        Write-Host "❌ 库存初始化失败: $($response.message)"
    }
} catch {
    Write-Host "❌ 请求失败: $($_.Exception.Message)"
}

# 3. 查询库存
Write-Host "[3/6] 查询库存..."
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/goods/1/stock" -TimeoutSec 5
    if ($response.code -eq 200) {
        Write-Host "✅ 库存查询成功: $($response.data.stock)"
    } else {
        Write-Host "❌ 库存查询失败"
    }
} catch {
    Write-Host "❌ 请求失败"
}

# 4. 获取秒杀路径
Write-Host "[4/6] 获取秒杀路径..."
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/seckill/1/path?userId=10001" -TimeoutSec 5
    if ($response.code -eq 200) {
        $global:path = $response.data.path
        Write-Host "✅ 获取路径: $path"
    } else {
        Write-Host "❌ 获取路径失败"
        exit
    }
} catch {
    Write-Host "❌ 请求失败"
    exit
}

# 5. 秒杀下单
Write-Host "[5/6] 秒杀下单..."
try {
    $body = @{
        userId = 10001
        path = $path
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/seckill/1/order" -Method POST -ContentType "application/json" -Body $body -TimeoutSec 5
    if ($response.code -eq 200) {
        Write-Host "✅ 秒杀成功: $($response.data.orderNo)"
    } else {
        Write-Host "❌ 秒杀失败: $($response.message)"
    }
} catch {
    Write-Host "❌ 请求失败: $($_.Exception.Message)"
}

# 6. 重复下单（测试幂等）
Write-Host "[6/6] 测试幂等性..."
try {
    $response2 = Invoke-RestMethod -Uri "$BASE_URL/api/seckill/1/path?userId=10001" -TimeoutSec 5
    $path2 = $response2.data.path
    
    $body2 = @{
        userId = 10001
        path = $path2
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/seckill/1/order" -Method POST -ContentType "application/json" -Body $body2 -TimeoutSec 5
    if ($response.code -eq 400) {
        Write-Host "✅ 幂等性正常: $($response.message)"
    } else {
        Write-Host "❌ 幂等性测试异常"
    }
} catch {
    Write-Host "❌ 请求失败"
}

Write-Host ""
Write-Host "============================================"
Write-Host "测试完成！去 MySQL 验证数据："
Write-Host "SELECT * FROM seckill_order;"
Write-Host "============================================"

# 暂停等待用户按键
Write-Host ""
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
