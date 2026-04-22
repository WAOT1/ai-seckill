# AI 智能秒杀助手

基于 Spring Boot + DeepSeek AI 的智能秒杀系统，实现高并发库存扣减与 AI 辅助决策。

## 功能特性

- ✅ **高并发秒杀**：基于 Redis + Lua 脚本实现原子库存扣减
- ✅ **AI 智能客服**：集成 DeepSeek Function Calling，AI 可调用系统函数获取真实数据
- ✅ **AI 预测补货**：分析历史数据，预测下次抢购时间
- ✅ **AI 反欺诈**：基于用户行为数据，识别脚本抢购和黄牛行为
- ✅ **限流防刷**：滑动窗口限流，防止恶意请求
- ✅ **数据持久化**：MyBatis-Plus + MySQL，订单落库不丢失
- ✅ **降级策略**：Redis 不可用时自动降级到内存模式

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.1.12 | 后端框架 |
| MyBatis-Plus | 3.5.4.1 | ORM 框架 |
| Redis | 7.x | 库存缓存、限流 |
| MySQL | 8.x | 数据持久化 |
| DeepSeek API | - | AI 能力 |
| Maven | - | 构建工具 |

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARACTER SET utf8mb4;
USE seckill;

CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    pay_time DATETIME,
    INDEX idx_user (user_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3. 配置

修改 `src/main/resources/application.yml`：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
  datasource:
    url: jdbc:mysql://localhost:3306/seckill?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码

deepseek:
  api-key: sk-你的DeepSeekKey
```

### 4. 运行

```bash
# 1. 启动 Redis
redis-server

# 2. 编译运行
mvn spring-boot:run

# 3. 测试
curl http://localhost:8080/api/ai/test
```

## API 文档

### 基础接口

#### 健康检查
```http
GET /api/ai/test
```

### 商品管理

#### 初始化库存
```http
POST /api/goods/{goodsId}/init?stock=100
```

#### 查询库存
```http
GET /api/goods/{goodsId}/stock
```

### 秒杀接口

#### 获取秒杀路径
```http
GET /api/seckill/{goodsId}/path?userId=10001
```

#### 秒杀下单
```http
POST /api/seckill/{goodsId}/order
Content-Type: application/json

{
  "userId": 10001,
  "path": "a1b2c3d4e5f6"
}
```

### AI 接口

#### 普通对话
```http
GET /api/ai/chat?q=你好
```

#### 智能客服（Function Calling）
```http
GET /api/ai/assistant?q=AJ1还有货吗
```

#### AI 预测
```http
GET /api/ai/predict/{goodsId}
```

#### AI 风控检查
```http
POST /api/ai/risk-check
Content-Type: application/json

{
  "userId": 10001,
  "clickCount": 50,
  "orderCount": 5
}
```

## 核心设计

### 1. 高并发扣库存（Redis + Lua）

```java
// Lua 脚本保证原子性
local stock = redis.call('get', KEYS[1]);
if stock == false then return -1; end;
local num = tonumber(stock);
if num <= 0 then return 0; end;
redis.call('decr', KEYS[1]);
return num;
```

**优势：**
- 原子操作，避免超卖
- 性能极高（纯内存操作）
- 支持降级（Redis 不可用时切内存）

### 2. Function Calling（AI 调用系统函数）

```
用户问："AJ1 还有货吗？"

AI → 识别需要查库存 → 调用 query_stock(goodsId=1)
系统 → 执行函数 → 返回 {"stock": 23}
AI → 基于真实数据回答 → "还剩 23 双，建议尽快下单"
```

**优势：**
- AI 不瞎编数字
- 实时查询真实数据
- 可扩展更多函数

### 3. 滑动窗口限流

```
Redis ZSet 实现：
- 记录每个请求的时间戳
- 移除窗口外的记录
- 统计窗口内请求数
- 超过阈值则拒绝
```

**优势：**
- 精确控制流量
- 分布式生效（基于 Redis）
- 误杀率低

## 项目结构

```
ai-seckill/
├── src/main/java/com/example/aiseckill/
│   ├── config/              # 配置类
│   ├── controller/          # API 接口
│   ├── domain/
│   │   ├── dto/             # 请求/响应 DTO
│   │   └── entity/          # 数据库实体
│   ├── interceptor/         # 拦截器（限流）
│   ├── mapper/              # MyBatis-Plus Mapper
│   ├── service/             # 业务逻辑
│   └── AiSeckillApplication.java
├── src/main/resources/
│   ├── application.yml      # 配置文件
│   └── sql/
│       └── init.sql         # 数据库初始化
└── pom.xml
```

## 部署

### Docker

```bash
docker-compose up -d
```

### 云服务器

```bash
# 1. 打包
mvn clean package -DskipTests

# 2. 上传 jar 到服务器
scp target/ai-seckill-0.0.1-SNAPSHOT.jar root@你的IP:/opt/

# 3. 启动
nohup java -jar ai-seckill-0.0.1-SNAPSHOT.jar &
```

## 后续优化

- [ ] 接入 RabbitMQ 异步下单
- [ ] 集成支付回调
- [ ] 前端页面（Vue/React）
- [ ] 监控大盘（Prometheus + Grafana）
- [ ] 压力测试（JMeter）

## License

MIT
