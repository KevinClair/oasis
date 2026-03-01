-- 更新应用管理菜单数据
-- 修复路由名称、路径、组件和国际化key

UPDATE `menu`
SET
    `route_name` = 'application',
    `route_path` = '/application',
    `component` = 'layout.base$view.application',
    `i18n_key` = 'route.application',
    `update_by` = 'system',
    `update_time` = NOW()
WHERE `route_name` = 'manage_application'
   OR `i18n_key` = 'route.manage_application'
   OR `route_name` = 'application';

-- 查询更新结果
SELECT * FROM `menu` WHERE `route_name` = 'application';

