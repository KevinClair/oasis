# HA_DESIGN_V3

## 目标
- 调度中心任一节点故障不影响整体调度
- 15 秒内完成分片接管

## 机制
- `shard_lease` 记录 owner、lease_expire_at
- 节点每 5 秒续约
- 过期 lease 可被其他节点抢占
- 节点优雅下线进入 drain 模式，主动释放 lease

## 当前实现状态（2026-03-07）
- 已实现 `scheduler_node` 心跳上报与超时下线。
- 已支持稳定节点标识（默认 `host:port`，可配置 `scheduler-node-id`）。
- 已实现 `ShardLeaseCoordinator` 周期续租/过期抢占。
- 已实现“仅扫描本节点持有分片”的调度扫描逻辑。
- 已实现优雅下线主动释放 lease（drain）。

## 风险点
- 时钟偏差
- 长事务导致 lease 续约失败
- 热点分片
