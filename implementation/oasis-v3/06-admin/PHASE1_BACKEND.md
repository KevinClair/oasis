# PHASE1_BACKEND

## 一期后端交付
- 调度域模型（任务、调度、日志、告警）
- 数据库扫描 + CAS 抢占调度能力（`job_schedule.version`）
- 执行器路由下发能力（ROUND/RANDOM/FAILOVER/BROADCAST）
- 注册/心跳/回调处理
- 应用默认告警模板 API
- 任务告警策略与事件 API

## 已实现执行链路
1. `ScheduleScannerService` 扫描到期任务。
2. 抢占成功后触发 `JobTriggerExecutorService`。
3. 写入 `job_fire_log(PENDING)`。
4. 路由选择在线 `executor_node` 并 HTTP 下发 `/oasis-executor/invoke`。
5. 下发成功更新 `job_fire_log(RUNNING)`，失败更新 `job_fire_log(FAILED)`。
6. 执行器回调 `callback/result` 与 `callback/log` 更新最终状态和分片日志。

## 已知一期限制
- `BROADCAST` 在一期单 `fireLog` 模型下暂降级为单节点执行，避免多节点回调覆盖同一日志记录。

## 关键约束
- 表名无前缀
- 注册前校验 appCode/appKey
- 任务来源一期仅控制台
