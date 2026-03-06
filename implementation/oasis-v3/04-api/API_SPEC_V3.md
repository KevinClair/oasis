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

### 应用默认告警模板
- `GET /schedule/app/{appCode}/alarm-template`
- `POST /schedule/app/alarm-template/save`

### 任务告警策略
- `GET /schedule/job/{jobId}/alarm-policy`
- `POST /schedule/job/alarm-policy/save`

### 任务告警事件
- `POST /schedule/job/{jobId}/alarm-events/list`
- `GET /schedule/alarm-event/{eventId}`

## Executor 协议（一期）

### Admin -> Executor
- `POST /oasis-executor/invoke`

### Executor -> Admin
- `POST /executor/registry/register`
- `POST /executor/registry/heartbeat`
- `POST /executor/callback/result`
- `POST /executor/callback/log`

## 鉴权
- 一期采用 `AppKey + HMAC`。
