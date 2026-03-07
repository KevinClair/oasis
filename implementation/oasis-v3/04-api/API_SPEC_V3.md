# API_SPEC_V3

## Admin API（一期）

### 任务管理
- `POST /schedule/job/list`
- `POST /schedule/job/save`
- `POST /schedule/job/enable`
- `POST /schedule/job/trigger`

### 调度日志
- `POST /schedule/log/list`
- `GET /schedule/log/{id}`

### 调度补偿队列（一期）

- `GET /schedule/dispatch/overview`
- `POST /schedule/dispatch/list`

### 应用默认告警模板
- `GET /schedule/app/{appCode}/alarm-template`
- `POST /schedule/app/alarm-template/save`

### 任务告警策略
- `GET /schedule/job/{jobId}/alarm-policy`
- `POST /schedule/job/alarm-policy/save`

### 任务告警事件
- `POST /schedule/job/{jobId}/alarm-events/list`
- `POST /schedule/alarm-events/list`
- `GET /schedule/alarm-event/{eventId}`

## Executor 协议（一期）

### Admin -> Executor
- `POST /{contextPath}/invoke`（由 starter 内置 Netty HTTP Server 提供，默认 `contextPath=/oasis-executor`）

### Executor -> Admin
- `POST /executor/registry/register`
- `POST /executor/registry/heartbeat`
- `POST /executor/callback/result`
- `POST /executor/callback/log`

## 鉴权
- 一期采用 `AppKey + HMAC`。
- Admin -> Executor 也使用同一组签名头：
  - `X-Oasis-App-Code`
  - `X-Oasis-Timestamp`
  - `X-Oasis-Nonce`
  - `X-Oasis-Signature`
- 执行器请求头要求：
  - `X-Oasis-App-Code`
  - `X-Oasis-Timestamp`
  - `X-Oasis-Nonce`
  - `X-Oasis-Signature`
- Admin 校验逻辑：
  - 时间戳偏移窗口校验
  - nonce 防重放
  - HMAC 签名校验
  - 回调 `fireLog` 归属校验（防跨应用回调）
  - `callback/result` 旧 attempt 回调幂等忽略（防乱序覆盖）
