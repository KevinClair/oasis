-- 创建角色菜单关联表
CREATE TABLE IF NOT EXISTS `role_menu`
(
    `id`          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`     BIGINT(20) NOT NULL COMMENT '角色ID',
    `menu_id`     BIGINT(20) NOT NULL COMMENT '菜单ID',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY           `idx_role_id` (`role_id`),
    KEY           `idx_menu_id` (`menu_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色菜单关联表';

-- 为超级管理员角色初始化菜单权限（假设角色ID为1）
-- 这里需要根据实际的菜单ID进行调整
INSERT INTO `role_menu` (`role_id`, `menu_id`, `create_by`)
SELECT 1, id, 'system'
FROM `menu`
WHERE status = 1
ON DUPLICATE KEY UPDATE create_time = create_time;

