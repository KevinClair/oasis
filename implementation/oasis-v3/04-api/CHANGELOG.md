# API CHANGELOG

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
