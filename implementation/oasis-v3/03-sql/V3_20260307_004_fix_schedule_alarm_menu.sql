-- V3 20260307 fix schedule alarm menu route naming & visibility
-- 目标：
-- 1) 修正旧错误命名：schedule_job-alarm-events -> schedule_job_alarm-events
-- 2) 修正组件/i18n key：view.schedule_job_alarm-events -> view.schedule_job_alarm-events
-- 3) 修正路径为可直接访问菜单页：/schedule/job/alarm-events
-- 4) 确保补偿队列菜单存在

SET @schedule_menu_id := (SELECT id
                          FROM menu
                          WHERE route_name = 'schedule'
                          LIMIT 1);

-- 确保补偿队列菜单存在
INSERT INTO menu (parent_id, menu_type, menu_name, route_name, route_path, component,
                  icon_type, icon, i18n_key, `order`, keep_alive, constant,
                  hide_in_menu, multi_tab, status, create_by, create_time, update_by, update_time)
SELECT @schedule_menu_id,
       2,
       '补偿队列',
       'schedule_dispatch',
       '/schedule/dispatch',
       'view.schedule_dispatch',
       '1',
       'mdi:database-sync-outline',
       'route.schedule_dispatch',
       3,
       0,
       0,
       0,
       0,
       1,
       'system',
       NOW(),
       'system',
       NOW()
FROM DUAL
WHERE @schedule_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1
                  FROM menu
                  WHERE route_name = 'schedule_dispatch');

SET @old_alarm_menu_id := (SELECT id
                           FROM menu
                           WHERE route_name = 'schedule_job-alarm-events'
                           LIMIT 1);
SET @new_alarm_menu_id := (SELECT id
                           FROM menu
                           WHERE route_name = 'schedule_job_alarm-events'
                           LIMIT 1);

-- 迁移旧菜单权限到新菜单（若新菜单已存在）
INSERT IGNORE INTO role_menu (role_id, menu_id, create_by, create_time)
SELECT role_id, @new_alarm_menu_id, create_by, NOW()
FROM role_menu
WHERE @old_alarm_menu_id IS NOT NULL
  AND @new_alarm_menu_id IS NOT NULL
  AND menu_id = @old_alarm_menu_id;

-- 删除旧菜单权限绑定
DELETE
FROM role_menu
WHERE @old_alarm_menu_id IS NOT NULL
  AND menu_id = @old_alarm_menu_id;

-- 若新菜单不存在，直接将旧菜单重命名为新菜单
UPDATE menu
SET route_name   = 'schedule_job_alarm-events',
    route_path   = '/schedule/job/alarm-events',
    component    = 'view.schedule_job_alarm-events',
    i18n_key     = 'route.schedule_job_alarm-events',
    hide_in_menu = 0,
    parent_id    = COALESCE(@schedule_menu_id, parent_id),
    `order`      = 4,
    update_by    = 'system',
    update_time  = NOW()
WHERE route_name = 'schedule_job-alarm-events'
  AND @new_alarm_menu_id IS NULL;

-- 若新菜单已存在，删除旧菜单脏数据
DELETE
FROM menu
WHERE route_name = 'schedule_job-alarm-events'
  AND @new_alarm_menu_id IS NOT NULL;

-- 统一新菜单字段（路径、组件、i18n、可见性）
UPDATE menu
SET route_path   = '/schedule/job/alarm-events',
    component    = 'view.schedule_job_alarm-events',
    i18n_key     = 'route.schedule_job_alarm-events',
    hide_in_menu = 0,
    parent_id    = COALESCE(@schedule_menu_id, parent_id),
    `order`      = 4,
    update_by    = 'system',
    update_time  = NOW()
WHERE route_name = 'schedule_job_alarm-events';
