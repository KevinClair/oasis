-- 插入应用管理菜单（一级目录，与首页同级）
-- 注意：此SQL脚本用于在动态路由模式下初始化应用管理菜单
-- 执行前请确保menu表已存在

INSERT INTO `menu` (
    `parent_id`,
    `menu_type`,
    `menu_name`,
    `route_name`,
    `route_path`,
    `component`,
    `icon_type`,
    `icon`,
    `i18n_key`,
    `order`,
    `keep_alive`,
    `constant`,
    `hide_in_menu`,
    `multi_tab`,
    `status`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`
)
VALUES (
    0,                              -- parent_id: 0表示一级菜单
    2,                              -- menu_type: 2表示菜单（不是目录）
    '应用管理',                      -- menu_name: 菜单名称
    'application',                  -- route_name: 路由名称
    '/application',                 -- route_path: 路由路径
    'layout.base$view.manage_application',  -- component: 组件路径
    '1',                            -- icon_type: 1表示iconify图标
    'mdi:application-cog',          -- icon: 图标名称
    'route.application',            -- i18n_key: 国际化key
    2,                              -- order: 排序（在首页之后）
    0,                              -- keep_alive: 0表示不缓存
    0,                              -- constant: 0表示非常量路由
    0,                              -- hide_in_menu: 0表示在菜单中显示
    0,                              -- multi_tab: 0表示不支持多tab
    1,                              -- status: 1表示启用
    'system',                       -- create_by: 创建人
    NOW(),                          -- create_time: 创建时间
    'system',                       -- update_by: 更新人
    NOW()                           -- update_time: 更新时间
);

-- 查询插入结果
SELECT * FROM `menu` WHERE `route_name` = 'application';

