# API CHANGELOG

## 2026-03-06
- 新增调度执行闭环实现（Admin 内核侧）：
  - 调度扫描器：按 `job_schedule.next_trigger_time` 扫描到期任务并触发
  - CAS 抢占：`job_schedule` 基于 `version + next_trigger_time` 抢占更新，避免多节点重复触发
  - 节点摘除：按心跳超时将 `executor_node.status` 置为 `OFFLINE`
  - 路由下发：支持 `ROUND` / `RANDOM` / `FAILOVER`，`BROADCAST` 一期安全降级为单节点执行
- 任务触发链路变更：
  - `POST /schedule/job/trigger` 从“仅写日志”升级为“写日志 + 下发执行器 + 更新日志状态”
  - 初始状态：`PENDING`；下发成功转 `RUNNING`；下发失败转 `FAILED`
- Starter 协议处理增强：
  - 客户端调用 Admin 后不再仅看 HTTP 200；改为校验响应 `code == 0000`
  - `RestTemplate` 增加连接/读取超时配置生效

## 2026-03-04
- 初始化 V3 API 文档与协议边界。
- 新增一期 Admin API 清单（任务/日志/告警策略/告警事件）。
- 新增一期 Executor 协议清单（注册、心跳、执行回调、日志回调）。
- 预留二期 OpenAPI 端点草案。
- 新增后端接口初版实现：
  - `POST /schedule/job/list`
  - `POST /schedule/job/save`
  - `POST /schedule/job/enable`
  - `POST /schedule/job/trigger`
  - `POST /schedule/log/list`
  - `GET /schedule/log/{id}`
  - `GET /schedule/app/{appCode}/alarm-template`
  - `POST /schedule/app/alarm-template/save`
  - `GET /schedule/job/{jobId}/alarm-policy`
  - `POST /schedule/job/alarm-policy/save`
  - `POST /schedule/job/{jobId}/alarm-events/list`
  - `GET /schedule/alarm-event/{eventId}`
  - `POST /executor/registry/register`
  - `POST /executor/registry/heartbeat`
  - `POST /executor/callback/result`
  - `POST /executor/callback/log`
