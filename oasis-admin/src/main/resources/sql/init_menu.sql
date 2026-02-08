-- 创建菜单表
CREATE TABLE IF NOT EXISTS `menu`
(
    `id`
    BIGINT
(
    20
) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT
(
    20
) DEFAULT 0 COMMENT '父级菜单ID，0表示顶级菜单',
    `menu_type` TINYINT
(
    1
) NOT NULL COMMENT '菜单类型：1-目录，2-菜单',
    `menu_name` VARCHAR
(
    50
) NOT NULL COMMENT '菜单名称',
    `route_name` VARCHAR
(
    100
) NOT NULL COMMENT '路由名称',
    `route_path` VARCHAR
(
    200
) NOT NULL COMMENT '路由路径',
    `path_param` VARCHAR
(
    200
) DEFAULT NULL COMMENT '路径参数',
    `component` VARCHAR
(
    200
) DEFAULT NULL COMMENT '组件名称',
    `icon_type` VARCHAR
(
    10
) DEFAULT '1' COMMENT '图标类型：1-iconify图标，2-本地图标',
    `icon` VARCHAR
(
    100
) DEFAULT NULL COMMENT '图标',
    `local_icon` VARCHAR
(
    100
) DEFAULT NULL COMMENT '本地图标',
    `i18n_key` VARCHAR
(
    100
) DEFAULT NULL COMMENT '国际化key',
    `order` INT
(
    11
) DEFAULT 0 COMMENT '排序',
    `keep_alive` TINYINT
(
    1
) DEFAULT 0 COMMENT '是否缓存：1-缓存(true)，0-不缓存(false)',
    `constant` TINYINT
(
    1
) DEFAULT 0 COMMENT '是否常量路由：1-是(true)，0-否(false)',
    `href` VARCHAR
(
    500
) DEFAULT NULL COMMENT '外链地址',
    `hide_in_menu` TINYINT
(
    1
) DEFAULT 0 COMMENT '是否隐藏菜单：1-隐藏(true)，0-显示(false)',
    `active_menu` VARCHAR
(
    100
) DEFAULT NULL COMMENT '激活的菜单key',
    `multi_tab` TINYINT
(
    1
) DEFAULT 0 COMMENT '支持多个tab页签：1-支持(true)，0-不支持(false)',
    `fixed_index_in_tab` INT
(
    11
) DEFAULT NULL COMMENT '固定在tab卡上的索引',
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
    UNIQUE KEY `uk_route_name`
(
    `route_name`
),
    KEY `idx_parent_id`
(
    `parent_id`
),
    KEY `idx_order`
(
    `order`
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT ='菜单表';

-- 插入初始化数据（示例）
INSERT INTO `menu` (`parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `component`, `icon_type`,
                    `icon`, `i18n_key`, `order`, `status`, `create_by`)
VALUES (0, 1, '系统管理', 'manage', '/manage', 'layout.base$view.manage', '1', 'carbon:cloud-service-management',
        'route.manage', 1, 1, 'system'),
       (1, 2, '用户管理', 'manage_user', '/manage/user', 'view.manage_user', '1', 'ic:round-manage-accounts',
        'route.manage_user', 1, 1, 'system'),
       (1, 2, '角色管理', 'manage_role', '/manage/role', 'view.manage_role', '1', 'carbon:user-role',
        'route.manage_role', 2, 1, 'system'),
       (1, 2, '菜单管理', 'manage_menu', '/manage/menu', 'view.manage_menu', '1', 'material-symbols:route',
        'route.manage_menu', 3, 1, 'system');

