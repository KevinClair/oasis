-- 创建数据库
CREATE DATABASE IF NOT EXISTS oasis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE oasis;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码',
    `nick_name`   VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `user_gender` VARCHAR(1)            DEFAULT NULL COMMENT '性别：1-男，2-女',
    `email`       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-启用(true)，0-禁用(false)',
    `create_by`   VARCHAR(50)           DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)           DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `nick_name`, `user_gender`, `email`, `phone`, `status`, `create_by`)
VALUES ('admin', '123456', '管理员', '1', 'admin@example.com', '13800138000', 1, 'system'),
       ('test', '123456', '测试用户', '1', 'test@example.com', '13800138001', 1, 'system'),
       ('user1', '123456', '普通用户', '2', 'user1@example.com', '13800138002', 1, 'system'),
       ('disabled', '123456', '禁用用户', '1', 'disabled@example.com', '13800138003', 0, 'system');


