-- 更新用户表结构，添加新增字段
-- 注意：执行前请备份数据库

-- 1. 添加昵称字段
ALTER TABLE `user` ADD COLUMN `nick_name` VARCHAR(50) NULL COMMENT '昵称' AFTER `password`;

-- 2. 添加性别字段
ALTER TABLE `user` ADD COLUMN `user_gender` VARCHAR(1) NULL COMMENT '性别：1-男，2-女' AFTER `nick_name`;

-- 3. 修改状态字段类型为TINYINT(1) - 对应Java的Boolean类型
-- 如果当前是INTEGER类型，直接修改
ALTER TABLE `user` MODIFY COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用(true)，0-禁用(false)';

-- 4. 添加创建人字段
ALTER TABLE `user` ADD COLUMN `create_by` VARCHAR(50) NULL COMMENT '创建人' AFTER `status`;

-- 5. 添加更新人字段
ALTER TABLE `user` ADD COLUMN `update_by` VARCHAR(50) NULL COMMENT '更新人' AFTER `create_time`;

-- 查看表结构
DESC `user`;



