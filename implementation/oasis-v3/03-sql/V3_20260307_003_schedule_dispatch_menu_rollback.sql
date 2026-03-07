-- rollback for V3_20260307_003_schedule_dispatch_menu.sql

DELETE
FROM menu
WHERE route_name = 'schedule_dispatch';

SET @schedule_menu_id := (SELECT id
                          FROM menu
                          WHERE route_name = 'schedule'
                          LIMIT 1);

UPDATE menu
SET `order`     = 3,
    update_by   = 'system',
    update_time = NOW()
WHERE route_name = 'schedule_job-alarm-events'
  AND parent_id = @schedule_menu_id;
