-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户工号（关联user表的user_id字段）',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 从user表的user_roles字段迁移数据到user_role表
-- 注意：这个迁移脚本假设user_roles字段存储的是角色编码的JSON数组
-- 如果需要迁移数据，请根据实际数据格式调整此脚本

-- 迁移完成后，可以选择删除user表的user_roles字段（建议先备份数据）
-- ALTER TABLE `user` DROP COLUMN `user_roles`;

