# task_20260307_scheduler_dispatch_delivery

## 本次交付范围

1. `shard_lease` drain 主动释放（节点关闭时主动释放租约并下线）。
2. `dispatch_queue` 可观测性接口：
    - `GET /schedule/dispatch/overview`
    - `POST /schedule/dispatch/list`
3. 运行期 SQL 迁移补齐（含 `scheduler_node` 相关落地脚本）。

## 需要执行的 SQL

1. 正向执行：
    - [
      `V3_20260307_002_scheduler_dispatch_runtime.sql`](/Users/kevin/develop/IdeaProjects/Oasis/implementation/oasis-v3/03-sql/V3_20260307_002_scheduler_dispatch_runtime.sql)
2. 回滚执行（如需）：
    - [
      `V3_20260307_002_scheduler_dispatch_runtime_rollback.sql`](/Users/kevin/develop/IdeaProjects/Oasis/implementation/oasis-v3/03-sql/V3_20260307_002_scheduler_dispatch_runtime_rollback.sql)

## SQL 重点说明

1. `scheduler_node`、`shard_lease`、`dispatch_queue` 使用 `CREATE TABLE IF NOT EXISTS`，可重复执行。
2. 新增索引：
    - `scheduler_node.idx_status_heartbeat`
    - `shard_lease.idx_owner_expire`
    - `dispatch_queue.idx_status_update_time`
3. 回滚脚本仅回滚新增索引，不删表，避免误删数据。

## 验证方案

1. 表与索引验证：
    - `SHOW CREATE TABLE scheduler_node;`
    - `SHOW CREATE TABLE shard_lease;`
    - `SHOW CREATE TABLE dispatch_queue;`
    - `SHOW INDEX FROM scheduler_node;`
    - `SHOW INDEX FROM shard_lease;`
    - `SHOW INDEX FROM dispatch_queue;`
2. 接口验证：
    - 调用 `GET /schedule/dispatch/overview`，确认返回
      `pendingCount/processingCount/successCount/deadCount/duePendingCount`。
    - 调用 `POST /schedule/dispatch/list`，按 `status/fireLogId/jobId` 条件分页查询。
3. drain 验证：
    - 启动 admin 节点后检查 `scheduler_node` 有 ONLINE 记录，`shard_lease` 有 owner 分片。
    - 停止该节点，确认：
        - `scheduler_node.status` 更新为 `OFFLINE`
        - `shard_lease.lease_expire_at` 被主动置为当前时间之前（可被其他节点即时接管）。
4. 重试链路验证：
    - 制造执行器下发失败场景，确认 `dispatch_queue` 产生 `PENDING` 记录。
    - 到达最大重试后，确认 `dispatch_queue.status=DEAD` 且 `job_fire_log.status=FAILED`。

## 代码位置（核心）

1. drain 逻辑：
    - [
      `ShardLeaseCoordinator.java`](/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/ShardLeaseCoordinator.java)
2. 可观测性接口：
    - [
      `ScheduleController.java`](/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/controller/ScheduleController.java)
    - [
      `ScheduleServiceImpl.java`](/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/ScheduleServiceImpl.java)
3. 队列表与查询：
    - [
      `DispatchQueueMapper.xml`](/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/resources/mapper/DispatchQueueMapper.xml)
