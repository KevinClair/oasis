-- 初始化调度中心菜单（动态路由）
-- 说明：
-- 1) 避免在 INSERT menu 时通过子查询读取 menu，防止报错：
--    You can't specify target table 'menu' for update in FROM clause
-- 2) 支持重复执行（幂等）

-- 1. 创建父菜单：schedule
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `component`,
    `icon_type`, `icon`, `i18n_key`, `order`, `keep_alive`, `constant`,
    `hide_in_menu`, `multi_tab`, `status`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
    0, 1, '调度中心', 'schedule', '/schedule', 'layout.base',
    '1', 'mdi:calendar-clock', 'route.schedule', 3, 0, 0,
    0, 0, 1, 'system', NOW(), 'system', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `menu` WHERE `route_name` = 'schedule'
);

-- 2. 读取父菜单ID
SET @schedule_menu_id := (
    SELECT `id` FROM `menu` WHERE `route_name` = 'schedule' LIMIT 1
);

-- 3. 创建子菜单：任务管理
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `component`,
    `icon_type`, `icon`, `i18n_key`, `order`, `keep_alive`, `constant`,
    `hide_in_menu`, `multi_tab`, `status`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
    @schedule_menu_id, 2, '任务管理', 'schedule_job', '/schedule/job', 'view.schedule_job',
    '1', 'mdi:clipboard-list-outline', 'route.schedule_job', 1, 0, 0,
    0, 0, 1, 'system', NOW(), 'system', NOW()
FROM DUAL
WHERE @schedule_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `menu` WHERE `route_name` = 'schedule_job'
  );

-- 4. 创建子菜单：调度日志
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `component`,
    `icon_type`, `icon`, `i18n_key`, `order`, `keep_alive`, `constant`,
    `hide_in_menu`, `multi_tab`, `status`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
    @schedule_menu_id, 2, '调度日志', 'schedule_log', '/schedule/log', 'view.schedule_log',
    '1', 'mdi:file-document-multiple-outline', 'route.schedule_log', 2, 0, 0,
    0, 0, 1, 'system', NOW(), 'system', NOW()
FROM DUAL
WHERE @schedule_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `menu` WHERE `route_name` = 'schedule_log'
  );

-- 5. 创建子菜单：补偿队列
INSERT INTO `menu` (`parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `component`,
                    `icon_type`, `icon`, `i18n_key`, `order`, `keep_alive`, `constant`,
                    `hide_in_menu`, `multi_tab`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
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
                  FROM `menu`
                  WHERE `route_name` = 'schedule_dispatch');

-- 6. 创建子菜单：任务告警事件
INSERT INTO `menu` (
    `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `component`,
    `icon_type`, `icon`, `i18n_key`, `order`, `keep_alive`, `constant`,
    `hide_in_menu`, `multi_tab`, `status`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT @schedule_menu_id,
       2,
       '告警事件',
       'schedule_job_alarm-events',
       '/schedule/job/alarm-events',
       'view.schedule_job_alarm-events',
    '1',
    'mdi:alarm-light-outline',
       'route.schedule_job_alarm-events',
    4,
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
                  FROM `menu`
                  WHERE `route_name` = 'schedule_job_alarm-events'
  );
