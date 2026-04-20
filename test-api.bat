@echo off
chcp 65001 >nul
echo ============================================
echo AI 智能秒杀助手 - API 测试脚本
echo ============================================
echo.

set BASE_URL=http://localhost:8080

:: 1. 健康检查
echo [1/6] 测试健康检查...
curl -s %BASE_URL%/api/ai/test | findstr "服务正常" >nul
if %errorlevel%==0 (
    echo ✅ 服务正常
) else (
    echo ❌ 服务未启动
    goto end
)

:: 2. 初始化库存
echo [2/6] 初始化库存...
curl -s -X POST "%BASE_URL%/api/goods/1/init?stock=10" | findstr "200" >nul
if %errorlevel%==0 (
    echo ✅ 库存初始化成功
) else (
    echo ❌ 库存初始化失败
)

:: 3. 查询库存
echo [3/6] 查询库存...
curl -s "%BASE_URL%/api/goods/1/stock" | findstr "stock" >nul
if %errorlevel%==0 (
    echo ✅ 库存查询成功
) else (
    echo ❌ 库存查询失败
)

:: 4. 获取秒杀路径
echo [4/6] 获取秒杀路径...
for /f "tokens=2 delims=:" %%a in ('curl -s "%BASE_URL%/api/seckill/1/path?userId=10001" ^| findstr "path"') do (
    set PATH_VALUE=%%a
)
set PATH_VALUE=%PATH_VALUE:"=%
set PATH_VALUE=%PATH_VALUE: =%
set PATH_VALUE=%PATH_VALUE:,=%
echo ✅ 获取路径: %PATH_VALUE%

:: 5. 秒杀下单
echo [5/6] 秒杀下单...
curl -s -X POST "%BASE_URL%/api/seckill/1/order" -H "Content-Type: application/json" -d "{\"userId\":10001,\"path\":\"%PATH_VALUE%\"}" | findstr "抢购成功" >nul
if %errorlevel%==0 (
    echo ✅ 秒杀成功
) else (
    echo ❌ 秒杀失败
)

:: 6. 重复下单（测试幂等）
echo [6/6] 测试幂等性...
for /f "tokens=2 delims=:" %%a in ('curl -s "%BASE_URL%/api/seckill/1/path?userId=10001" ^| findstr "path"') do (
    set PATH_VALUE2=%%a
)
set PATH_VALUE2=%PATH_VALUE2:"=%
set PATH_VALUE2=%PATH_VALUE2: =%
set PATH_VALUE2=%PATH_VALUE2:,=%
curl -s -X POST "%BASE_URL%/api/seckill/1/order" -H "Content-Type: application/json" -d "{\"userId\":10001,\"path\":\"%PATH_VALUE2%\"}" | findstr "已参与" >nul
if %errorlevel%==0 (
    echo ✅ 幂等性正常（重复下单被拒绝）
) else (
    echo ❌ 幂等性测试失败
)

echo.
echo ============================================
echo 测试完成！去 MySQL 验证数据：
echo SELECT * FROM seckill_order;
echo ============================================

:end
pause
