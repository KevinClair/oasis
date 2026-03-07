# SECURITY_PROTOCOL

## 鉴权方式
`AppKey + HMAC`。

## 签名建议串
按换行拼接：
`timestamp + "\n" + nonce + "\n" + method + "\n" + path + "\n" + bodyHash`

## 安全校验
- 时间窗校验
- nonce 防重放
- bodyHash 防篡改
- 执行日志回调归属校验（`fireLog` 必须归属于当前 appCode）

## 生效范围
- Executor -> Admin（注册、心跳、结果回调、日志回调）
- Admin -> Executor（`/invoke` 下发）
