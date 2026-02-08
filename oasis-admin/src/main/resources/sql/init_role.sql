-- 创建角色表
CREATE TABLE IF NOT EXISTS `role`
(
    `id`
    BIGINT
(
    20
) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR
(
    50
) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR
(
    50
) NOT NULL COMMENT '角色编码',
    `role_desc` VARCHAR
(
    200
) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT
(
    1
) NOT NULL DEFAULT 1 COMMENT '状态：1-启用(true)，0-禁用(false)',
    `create_by` VARCHAR
(
    50
) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR
(
    50
) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY
(
    `id`
),
    UNIQUE KEY `uk_role_code`
(
    `role_code`
),
    KEY `idx_role_name`
(
    `role_name`
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

-- 插入初始化数据
INSERT INTO `role` (`role_name`, `role_code`, `role_desc`, `status`, `create_by`)
VALUES ('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1, 'system'),
       ('管理员', 'ADMIN', '拥有系统管理权限', 1, 'system'),
       ('普通用户', 'USER', '普通用户权限', 1, 'system'),
       ('访客', 'GUEST', '只读访问权限', 0, 'system');

