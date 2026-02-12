-- 插入基础页面菜单数据
-- 包括：403、404、500、login、home等页面
-- 其中403、404、500为常量路由（constant=1）

-- 清空现有菜单数据（可选，谨慎使用）
-- TRUNCATE TABLE `menu`;

-- ============================================
-- 1. 常量路由（系统内置，所有用户可访问）
-- ============================================

-- 1.1 403 无权限页面
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    0, 2, '403', '403', '/403',
    'layout.base$view.403', '1', 'ic:baseline-block', 'route.403', 0,
    0, 1, 1, 1, 'system'
);

-- 1.2 404 页面不存在
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    0, 2, '404', '404', '/404',
    'layout.base$view.404', '1', 'ic:baseline-error-outline', 'route.404', 0,
    0, 1, 1, 1, 'system'
);

-- 1.3 500 服务器错误
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    0, 2, '500', '500', '/500',
    'layout.base$view.500', '1', 'ic:baseline-error', 'route.500', 0,
    0, 1, 1, 1, 'system'
);

-- 1.4 登录页面（常量路由，但不隐藏，用于路由切换）
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    0, 2, '登录', 'login', '/login',
    'layout.blank$view.login', '1', 'mdi:login', 'route.login', 0,
    0, 1, 1, 1, 'system'
);

-- ============================================
-- 2. 动态路由（需要权限控制）
-- ============================================

-- 2.1 首页
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `multi_tab`, `status`, `create_by`
) VALUES (
    0, 2, '首页', 'home', '/home',
    'layout.base$view.home', '1', 'mdi:home', 'route.home', 1,
    0, 0, 0, 1, 1, 'system'
);

-- 2.2 系统管理（目录）
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    0, 1, '系统管理', 'manage', '/manage',
    'layout.base', '1', 'carbon:cloud-service-management', 'route.manage', 2,
    0, 0, 0, 1, 'system'
);

-- 获取刚插入的系统管理菜单ID（用于子菜单）
SET @manage_id = LAST_INSERT_ID();

-- 2.3 用户管理
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    @manage_id, 2, '用户管理', 'manage_user', '/manage/user',
    'view.manage_user', '1', 'ic:round-manage-accounts', 'route.manage_user', 1,
    1, 0, 0, 1, 'system'
);

-- 2.4 角色管理
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    @manage_id, 2, '角色管理', 'manage_role', '/manage/role',
    'view.manage_role', '1', 'carbon:user-role', 'route.manage_role', 2,
    1, 0, 0, 1, 'system'
);

-- 2.5 菜单管理
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `status`, `create_by`
) VALUES (
    @manage_id, 2, '菜单管理', 'manage_menu', '/manage/menu',
    'view.manage_menu', '1', 'material-symbols:route', 'route.manage_menu', 3,
    1, 0, 0, 1, 'system'
);

-- ============================================
-- 3. 其他内置页面（可选）
-- ============================================

-- 3.1 iframe页面（如果需要）
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`,
    `path_param`, `component`, `icon_type`, `icon`, `i18n_key`, `order`,
    `keep_alive`, `constant`, `hide_in_menu`, `multi_tab`, `status`, `create_by`
) VALUES (
    0, 2, 'iframe页面', 'iframe-page', '/iframe-page/:url',
    '/:url', 'layout.base$view.iframe-page', '1', 'mdi:newspaper-variant-outline', 'route.iframe-page', 99,
    1, 1, 1, 1, 1, 'system'
);

-- ============================================
-- 查询验证
-- ============================================

-- 查询所有菜单
SELECT
    id,
    parent_id,
    menu_type,
    menu_name,
    route_name,
    route_path,
    constant,
    hide_in_menu,
    status
FROM `menu`
ORDER BY parent_id, `order`;

-- 查询常量路由
SELECT
    menu_name,
    route_name,
    route_path,
    constant
FROM `menu`
WHERE constant = 1;

-- 查询动态路由
SELECT
    menu_name,
    route_name,
    route_path,
    constant
FROM `menu`
WHERE constant = 0;

