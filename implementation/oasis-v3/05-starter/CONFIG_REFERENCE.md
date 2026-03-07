# CONFIG_REFERENCE

## 配置前缀
`oasis.scheduler`

## 配置项
- `oasis.scheduler.enabled`: 是否启用 starter（默认 `true`）
- `oasis.scheduler.app-code`: 应用编码（必填，需先在 Admin 创建并启用）
- `oasis.scheduler.app-key`: 应用密钥（必填）
- `oasis.scheduler.admin.base-url`: Admin 地址（默认 `http://127.0.0.1:8080`）
- `oasis.scheduler.admin.connect-timeout-ms`: 注册/回调连接超时（默认 `2000`）
- `oasis.scheduler.admin.read-timeout-ms`: 注册/回调读取超时（默认 `3000`）
- `oasis.scheduler.server.port`: 客户端接收端口（默认 `19091`）
- `oasis.scheduler.server.context-path`: 客户端上下文（默认 `/oasis-executor`）
- `oasis.scheduler.server.verify-invoke-signature`: 是否校验 admin 下发签名（默认 `true`）
- `oasis.scheduler.heartbeat.interval-ms`: 心跳间隔（默认 `2000`）
- `oasis.scheduler.heartbeat.miss-threshold`: 丢失阈值（默认 `2`，一期预留）
- `oasis.scheduler.worker.core-size`: 执行线程池核心线程（默认 `4`）
- `oasis.scheduler.worker.max-size`: 执行线程池最大线程（默认 `16`）
- `oasis.scheduler.worker.queue-capacity`: 执行队列大小（默认 `1000`）
- `oasis.scheduler.callback.batch-size`: 回调批次大小（默认 `100`，一期预留）
- `oasis.scheduler.callback.flush-interval-ms`: 回调刷新周期（默认 `1000`，一期预留）
- `oasis.scheduler.security.sign-algorithm`: 签名算法（默认 `HMAC-SHA256`）
- `oasis.scheduler.security.clock-skew-ms`: 时间偏移容忍（默认 `30000`）
- `oasis.scheduler.security.nonce-expire-ms`: nonce 过期窗口（默认 `120000`）
- `oasis.scheduler.security.nonce-max-size`: nonce 本地缓存上限（默认 `20000`）

## 行为说明
- starter 对 Admin 接口成功判定为：HTTP 2xx 且响应体 `code == 0000`。
- `/invoke` 接口由 starter 内置 Netty HTTP Server 提供，Admin 默认访问地址：
  - `http://{executorAddress}{context-path}/invoke`
- 默认开启 `/invoke` 验签，只有合法签名请求会被接收并入队执行。

## 示例
```yaml
oasis:
  scheduler:
    enabled: true
    app-code: order-service
    app-key: ${OASIS_APP_KEY}
    admin:
      base-url: http://127.0.0.1:8080
      connect-timeout-ms: 2000
      read-timeout-ms: 3000
    server:
      port: 19091
      context-path: /oasis-executor
      verify-invoke-signature: true
    heartbeat:
      interval-ms: 2000
      miss-threshold: 2
    worker:
      core-size: 4
      max-size: 16
      queue-capacity: 1000
    security:
      clock-skew-ms: 30000
      nonce-expire-ms: 120000
      nonce-max-size: 20000
```
