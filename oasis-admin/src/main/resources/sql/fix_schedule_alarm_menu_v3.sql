-- 修复调度菜单命名与可见性（与 implementation/oasis-v3/03-sql/V3_20260307_004 对应）

SET @schedule_menu_id := (SELECT id
                          FROM menu
                          WHERE route_name = 'schedule'
                          LIMIT 1);

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
  AND NOT EXISTS (SELECT 1 FROM menu WHERE route_name = 'schedule_dispatch');

SET @old_alarm_menu_id := (SELECT id
                           FROM menu
                           WHERE route_name = 'schedule_job-alarm-events'
                           LIMIT 1);
SET @new_alarm_menu_id := (SELECT id
                           FROM menu
                           WHERE route_name = 'schedule_job_alarm-events'
                           LIMIT 1);

INSERT IGNORE INTO role_menu (role_id, menu_id, create_by, create_time)
SELECT role_id, @new_alarm_menu_id, create_by, NOW()
FROM role_menu
WHERE @old_alarm_menu_id IS NOT NULL
  AND @new_alarm_menu_id IS NOT NULL
  AND menu_id = @old_alarm_menu_id;

DELETE
FROM role_menu
WHERE @old_alarm_menu_id IS NOT NULL
  AND menu_id = @old_alarm_menu_id;

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

DELETE
FROM menu
WHERE route_name = 'schedule_job-alarm-events'
  AND @new_alarm_menu_id IS NOT NULL;

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

