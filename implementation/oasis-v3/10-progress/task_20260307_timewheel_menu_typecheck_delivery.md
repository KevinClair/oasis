# task_20260307_timewheel_menu_typecheck_delivery

## 交付目标

1. 将“自研时间轮”落地为独立调度组件，不再由扫描器直接驱动主触发链路。
2. 修复前端 2 个历史 typecheck 问题。
3. 修复菜单国际化 key 与动态路由命名不一致导致的菜单显示异常。
4. 修复“补偿队列/告警事件”授权后左侧菜单不可见问题。

## 本次实现内容

- 独立时间轮组件：
    - `ScheduleTimeWheel`：槽位、轮次、去重、tick 消费。
    - `ScheduleTimeWheelDispatcher`：窗口预加载 + 到期触发 + CAS 抢占。
    - `ScheduleScannerService`：改为兜底模式（仅在 `time-wheel-enabled=false` 时启用扫描触发）。
- 告警事件菜单修复：
    - 统一路由名：`schedule_job_alarm-events`
    - 统一组件名：`view.schedule_job_alarm-events`
    - 菜单路径调整：`/schedule/job/alarm-events`
    - 菜单可见：`hide_in_menu=0`
    - 修复旧错误数据并迁移 `role_menu` 绑定
- 告警事件页面增强：
    - 页面支持全局列表 + `jobId` 筛选
    - 新增后端接口：`POST /schedule/alarm-events/list`
- 前端 typecheck 修复：
    - `application-search.vue`：布尔筛选值类型兼容
    - `user-search.vue`：`userId` 输入改为字符串输入

## 需要执行的 SQL

1. 正向：
    - `implementation/oasis-v3/03-sql/V3_20260307_004_fix_schedule_alarm_menu.sql`
2. 可选（与上面同逻辑，放在 admin 目录便于运维）：
    - `oasis-admin/src/main/resources/sql/fix_schedule_alarm_menu_v3.sql`

## 回滚 SQL

1. 执行：
    - `implementation/oasis-v3/03-sql/V3_20260307_004_fix_schedule_alarm_menu_rollback.sql`

## 验证方案

1. 时间轮验证
    - 确认配置 `oasis.scheduler.runtime.time-wheel-enabled=true`。
    - 观察日志与数据库，任务可按计划触发，且无明显重复触发。
    - 将该开关置 `false` 后，确认扫描兜底链路可继续触发任务。
2. 菜单与权限验证
    - 给角色绑定 `schedule_dispatch` 与 `schedule_job_alarm-events`。
    - 登录后确认左侧展示“补偿队列”“告警事件”。
    - 菜单管理列表中“名称”列不再显示原始 key 字符串。
3. 告警事件页面验证
    - 访问 `/schedule/job/alarm-events`，可按 `jobId` 过滤查询。
    - 从任务页跳转 `告警事件` 时携带 `jobId` 查询参数并正常展示数据。
4. 编译与类型检查
    - `mvn -q -f oasis-admin/pom.xml -DskipTests compile`
    - `mvn -q -f oasis-scheduler-spring-boot-starter/pom.xml -DskipTests compile`
    - `pnpm -C oasis-web typecheck`
