# HA_DESIGN_V3

## 目标
- 调度中心任一节点故障不影响整体调度
- 15 秒内完成分片接管

## 机制
- `shard_lease` 记录 owner、lease_expire_at
- 节点每 5 秒续约
- 过期 lease 可被其他节点抢占
- 节点优雅下线进入 drain 模式，主动释放 lease

## 风险点
- 时钟偏差
- 长事务导致 lease 续约失败
- 热点分片
