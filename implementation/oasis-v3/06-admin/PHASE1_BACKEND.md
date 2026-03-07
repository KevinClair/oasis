# PHASE1_BACKEND

## 一期后端交付
- 调度域模型（任务、调度、日志、告警）
- `shard_lease` 分片租约调度能力（租约续约/过期抢占）
- 分片内数据库扫描 + CAS 抢占调度能力（`job_schedule.version`）
- 执行器路由下发能力（ROUND/RANDOM/FAILOVER/BROADCAST）
- 注册/心跳/回调处理
- 执行器 HMAC 鉴权（签名、时间窗、nonce 防重放）
- Admin -> Executor 下发签名（`/invoke` 请求头携带 `X-Oasis-*`）
- `dispatch_queue` 异步重试与失败补偿
- 应用默认告警模板 API
- 任务告警策略与事件 API

## 已实现执行链路
1. `ShardLeaseCoordinator` 续租并维护本节点分片所有权。
2. `ScheduleScannerService` 仅扫描当前节点持有分片内的到期任务。
3. 抢占成功后触发 `JobTriggerExecutorService`。
4. 写入 `job_fire_log(PENDING)`。
5. 路由选择在线 `executor_node` 并 HTTP 下发 `/oasis-executor/invoke`。
6. 下发成功更新 `job_fire_log(RUNNING)`，失败进入 `dispatch_queue` 异步重试。
7. 重试超过阈值后标记 `dispatch_queue(DEAD)` 并最终补偿为 `job_fire_log(FAILED)`。
8. 执行器回调 `callback/result` 与 `callback/log` 更新最终状态和分片日志。

## 已知一期限制
- `BROADCAST` 在一期单 `fireLog` 模型下暂降级为单节点执行，避免多节点回调覆盖同一日志记录。

## 关键约束
- 表名无前缀
- 注册前校验 appCode/appKey
- 任务来源一期仅控制台
