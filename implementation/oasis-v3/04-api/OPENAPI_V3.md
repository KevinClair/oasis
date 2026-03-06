# OPENAPI_V3（二期预留）

## 目标
支持客户端或外部系统动态创建、修改、删除任务。

## 预留端点
- `POST /openapi/v1/jobs`
- `PUT /openapi/v1/jobs/{jobId}`
- `DELETE /openapi/v1/jobs/{jobId}`
- `GET /openapi/v1/jobs/{jobId}`
- `POST /openapi/v1/jobs/list`

## 安全
- AK/SK + HMAC 签名
- 时间窗校验 + nonce 防重放
- 访问频率限制与审计日志
