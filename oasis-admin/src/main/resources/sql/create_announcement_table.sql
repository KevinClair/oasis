-- 创建公告表
CREATE TABLE IF NOT EXISTS `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `type` VARCHAR(20) NOT NULL COMMENT '公告类型：normal-普通，warning-警告，important-重要通知',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 插入公告管理菜单
INSERT INTO `menu` (`parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `component`,
                     `icon_type`, `icon`, `i18n_key`, `order`, `keep_alive`, `constant`, `status`,
                     `hide_in_menu`, `multi_tab`, `create_by`, `update_by`)
VALUES
    -- 找到系统管理的ID（假设为某个值，需要根据实际情况调整）
    ((SELECT id FROM (SELECT id FROM menu WHERE route_name = 'manage' LIMIT 1) AS tmp),
     2,
     '公告管理',
     'manage_announcement',
     '/manage/announcement',
     'view',
     '1',
     'mdi:bullhorn-outline',
     'route.manage_announcement',
     5,
     1,
     0,
     1,
     0,
     0,
     'admin',
     'admin');

