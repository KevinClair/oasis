# TECHNICAL_PLAN_V3

## 架构
- `oasis-admin`：调度中心（多实例）
- `oasis-scheduler-spring-boot-starter`：客户端
- `MySQL`：任务、租约、队列、日志、告警存储

## 核心模型
- 分片租约 + 本地时间轮
- 任务通过 `hash(jobId) % shardCount` 映射分片
- 节点抢占并续约分片 lease
- 到期任务写入 `job_fire_log` 与 `dispatch_queue`

## 一期通信
- Admin -> Client：HTTP invoke
- Client -> Admin：注册/心跳/结果回调/日志回调
- 下线快速感知：心跳 + 主动探测双判定
