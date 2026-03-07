# task_20260307_feature_1_5_8_delivery

## 本次交付范围（按 1 / 5 / 8）

1. Admin -> Executor 下发签名链路收口（运行期稳定性增强）。
5. 分片租约与路由下发链路优化（节点标识稳定、路由排序稳定）。
8. 补偿队列可观测性完善（后端接口 + 前端页面 + 动态菜单）。

## 代码变更摘要

- Admin
    - `SchedulerRuntimeProperties` 新增 `schedulerNodeId`（可选固定节点标识）。
    - `ShardLeaseCoordinator` 默认使用稳定 `host:port` 节点ID（未配置 `scheduler-node-id` 时）。
    - `ExecutorNodeMapper.xml` 在线节点排序改为 `id ASC`，保证 ROUND 路由基础顺序稳定。
    - `ExecutorGatewayServiceImpl#callbackResult` 增加 attempt 乱序幂等保护。
- Web
    - 新增页面：`/schedule/dispatch`（概览指标 + 列表查询）。
    - 新增 API：`fetchGetDispatchQueueOverview`、`fetchGetDispatchQueueList`。
    - 新增类型：`DispatchQueueOverview`、`DispatchQueue`、`DispatchQueueSearchParams`、`DispatchQueueList`。

## 需要执行的 SQL

1. 正向执行：
    - `implementation/oasis-v3/03-sql/V3_20260307_003_schedule_dispatch_menu.sql`
2. 如已执行过旧版菜单脚本，可重复执行该脚本（幂等）。

## 回滚 SQL

1. 执行：
    - `implementation/oasis-v3/03-sql/V3_20260307_003_schedule_dispatch_menu_rollback.sql`

## 验证方案

1. 菜单与路由验证
    - 登录后检查左侧是否出现“补偿队列”菜单（`/schedule/dispatch`）。
    - 打开页面后应可看到概览统计卡与列表。
2. 接口验证
    - `GET /schedule/dispatch/overview` 返回 `pendingCount/processingCount/successCount/deadCount/duePendingCount`。
    - `POST /schedule/dispatch/list` 支持按 `status/fireLogId/jobId` 过滤与分页。
3. 分片租约验证
    - 启动 Admin 后检查 `scheduler_node.node_id` 是否稳定（默认 `host:port` 或显式 `scheduler-node-id`）。
    - 关闭节点后检查 `shard_lease.owner_node_id` 被释放，`scheduler_node.status=OFFLINE`。
4. 回调幂等验证
    - 对同一个 `fireLogId`，先写入更大 `attemptNo` 的回调，再发送更小 `attemptNo` 回调；
    - 预期：旧 attempt 回调被忽略，不覆盖当前状态。

## 备注

- 如需让节点ID与部署实例名完全一致，建议在配置中显式设置：
    - `oasis.scheduler.runtime.scheduler-node-id=<stable-node-id>`
