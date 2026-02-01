-- 用户表字段调整SQL脚本
-- 注意：执行前请备份数据库

-- 1. 添加用户工号字段
ALTER TABLE `user` ADD COLUMN `user_id` BIGINT NULL COMMENT '用户工号' AFTER `id`;

-- 2. 添加用户账号字段
ALTER TABLE `user` ADD COLUMN `user_account` VARCHAR(50) NULL COMMENT '用户账号' AFTER `user_id`;

-- 3. 重命名username为user_name
ALTER TABLE `user` CHANGE COLUMN `username` `user_name` VARCHAR(50) NOT NULL COMMENT '用户名';

-- 4. 为user_id和user_account字段添加唯一索引
ALTER TABLE `user` ADD UNIQUE KEY `uk_user_id` (`user_id`);
ALTER TABLE `user` ADD UNIQUE KEY `uk_user_account` (`user_account`);

-- 5. 为user_name字段保留索引（如果不存在则添加）
-- ALTER TABLE `user` ADD KEY `idx_user_name` (`user_name`);

-- 查看表结构
DESC `user`;

-- 示例：更新现有数据（根据实际情况调整）
-- UPDATE `user` SET `user_id` = `id`, `user_account` = LOWER(REPLACE(`user_name`, ' ', '.')) WHERE `user_id` IS NULL;

