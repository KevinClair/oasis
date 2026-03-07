-- rollback for V3_20260307_004_fix_schedule_alarm_menu.sql

UPDATE menu
SET route_name   = 'schedule_job-alarm-events',
    route_path   = '/schedule/job/alarm-events/:jobId',
    component    = 'view.schedule_job-alarm-events',
    i18n_key     = 'route.schedule_job-alarm-events',
    hide_in_menu = 1,
    update_by    = 'system',
    update_time  = NOW()
WHERE route_name = 'schedule_job_alarm-events';

-- 若该脚本在回滚环境中创建了补偿队列菜单，可按需删除
DELETE
FROM menu
WHERE route_name = 'schedule_dispatch'
  AND route_path = '/schedule/dispatch'
  AND component = 'view.schedule_dispatch';
