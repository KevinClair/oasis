# FIELD_MAPPING

## 应用默认告警模板
- appCode -> `app_alarm_template.app_code`
- receivers -> `app_alarm_template.receivers`
- channels -> `app_alarm_template.channels`
- quietPeriodMinutes -> `app_alarm_template.quiet_period_minutes`

## 任务告警策略
- jobId -> `job_alarm_policy.job_id`
- inheritAppTemplate -> `job_alarm_policy.inherit_app_template`
- receivers -> `job_alarm_policy.receivers`
- channels -> `job_alarm_policy.channels`
- enabled -> `job_alarm_policy.enabled`
- quietPeriodMinutes -> `job_alarm_policy.quiet_period_minutes`
- failThreshold -> `job_alarm_policy.fail_threshold`
- timeoutSeconds -> `job_alarm_policy.timeout_seconds`

## 告警事件
- jobId -> `job_alarm_event.job_id`
- fireLogId -> `job_alarm_event.fire_log_id`
- notifyStatus -> `job_alarm_event.notify_status`
- 告警事件页筛选.jobId -> `job_alarm_event.job_id`（可选）

## 任务管理页
- appCode -> `job_info.app_code`
- jobName -> `job_info.job_name`
- handlerName -> `job_info.handler_name`
- scheduleType -> `job_info.schedule_type`
- scheduleConf -> `job_info.schedule_conf`
- routeStrategy -> `job_info.route_strategy`
- blockStrategy -> `job_info.block_strategy`
- retryCount -> `job_info.retry_count`
- timeoutSeconds -> `job_info.timeout_seconds`
- alarmInheritApp -> `job_info.alarm_inherit_app`
- nextTriggerTime -> `job_schedule.next_trigger_time`

## 调度日志页
- status -> `job_fire_log.status`
- triggerTime -> `job_fire_log.trigger_time`
- finishTime -> `job_fire_log.finish_time`
- executorAddress -> `job_fire_log.executor_address`
- traceId -> `job_fire_log.trace_id`
- detailDrawer.logId -> `job_fire_log.id`
- detailDrawer.errorMessage -> `job_fire_log.error_message`
- detailDrawer.attemptNo -> `job_fire_log.attempt_no`

## 补偿队列页

- pendingCount -> `COUNT(dispatch_queue.status='PENDING')`
- processingCount -> `COUNT(dispatch_queue.status='PROCESSING')`
- successCount -> `COUNT(dispatch_queue.status='SUCCESS')`
- deadCount -> `COUNT(dispatch_queue.status='DEAD')`
- duePendingCount -> `COUNT(dispatch_queue.status='PENDING' AND dispatch_queue.next_retry_time <= now)`
- fireLogId -> `dispatch_queue.fire_log_id`
- jobId -> `dispatch_queue.job_id`
- status -> `dispatch_queue.status`
- retryCount -> `dispatch_queue.retry_count`
- nextRetryTime -> `dispatch_queue.next_retry_time`
- payload -> `dispatch_queue.payload`
- targetAddress -> `dispatch_queue.target_address`
- updateTime -> `dispatch_queue.update_time`
