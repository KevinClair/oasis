-- V3 20260307 schedule dispatch menu
-- 作用：
-- 1) 在“调度中心”下新增“补偿队列”菜单
-- 2) 调整“告警事件”隐藏菜单顺序到 4（补偿队列使用顺序 3）

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
  AND NOT EXISTS (SELECT 1
                  FROM menu
                  WHERE route_name = 'schedule_dispatch');

UPDATE menu
SET `order`     = 4,
    update_by   = 'system',
    update_time = NOW()
WHERE route_name = 'schedule_job-alarm-events'
  AND parent_id = @schedule_menu_id;
