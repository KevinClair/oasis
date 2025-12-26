-- 创建数据库
CREATE
DATABASE IF NOT EXISTS oasis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
oasis;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`
    BIGINT
(
    20
) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR
(
    50
) NOT NULL COMMENT '用户名',
    `password` VARCHAR
(
    100
) NOT NULL COMMENT '密码',
    `email` VARCHAR
(
    100
) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR
(
    20
) DEFAULT NULL COMMENT '手机号',
    `status` INT
(
    1
) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY
(
    `id`
),
    UNIQUE KEY `uk_username`
(
    `username`
),
    KEY `idx_username`
(
    `username`
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`)
VALUES ('admin', '123456', 'admin@example.com', '13800138000', 1),
       ('test', '123456', 'test@example.com', '13800138001', 1),
       ('user1', '123456', 'user1@example.com', '13800138002', 1);

