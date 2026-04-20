-- 创建数据库
CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARACTER SET utf8mb4;
USE seckill;

-- 商品表
CREATE TABLE IF NOT EXISTS goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL COMMENT '商品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '原价',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    total_stock INT NOT NULL COMMENT '总库存',
    img_url VARCHAR(500) COMMENT '商品图片',
    status TINYINT DEFAULT 1 COMMENT '0-下架 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) UNIQUE NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT DEFAULT 1 COMMENT '数量',
    status TINYINT DEFAULT 0 COMMENT '0-未支付 1-已支付 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    pay_time DATETIME COMMENT '支付时间',
    INDEX idx_user (user_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据
INSERT INTO goods (id, name, price, seckill_price, total_stock, img_url, status) VALUES
(1, '限量版AJ1球鞋', 1499.00, 999.00, 100, 'https://example.com/aj1.jpg', 1),
(2, '茅台飞天53度', 2999.00, 1499.00, 50, 'https://example.com/maotai.jpg', 1),
(3, '华为Mate 70 Pro', 6999.00, 5999.00, 30, 'https://example.com/mate70.jpg', 1);
