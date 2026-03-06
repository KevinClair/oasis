-- rollback for V3_20260304_001_core_tables.sql

DROP TABLE IF EXISTS `job_alarm_notify_record`;
DROP TABLE IF EXISTS `job_alarm_event`;
DROP TABLE IF EXISTS `job_alarm_policy`;
DROP TABLE IF EXISTS `app_alarm_template`;
DROP TABLE IF EXISTS `executor_node`;
DROP TABLE IF EXISTS `job_log_chunk`;
DROP TABLE IF EXISTS `job_fire_log`;
DROP TABLE IF EXISTS `dispatch_queue`;
DROP TABLE IF EXISTS `shard_lease`;
DROP TABLE IF EXISTS `scheduler_node`;
DROP TABLE IF EXISTS `job_schedule`;
DROP TABLE IF EXISTS `job_info`;
