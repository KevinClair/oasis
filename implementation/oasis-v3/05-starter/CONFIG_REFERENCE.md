# CONFIG_REFERENCE

## 配置前缀
`oasis.scheduler`

## 配置项
- `oasis.scheduler.enabled`: 是否启用 starter
- `oasis.scheduler.app-code`: 应用编码
- `oasis.scheduler.app-key`: 应用密钥
- `oasis.scheduler.admin.base-url`: Admin 地址
- `oasis.scheduler.admin.connect-timeout-ms`: 连接超时
- `oasis.scheduler.admin.read-timeout-ms`: 读取超时
- `oasis.scheduler.server.port`: 客户端接收端口
- `oasis.scheduler.server.context-path`: 客户端上下文
- `oasis.scheduler.heartbeat.interval-ms`: 心跳间隔
- `oasis.scheduler.heartbeat.miss-threshold`: 丢失阈值
- `oasis.scheduler.worker.core-size`: 执行线程池核心线程
- `oasis.scheduler.worker.max-size`: 执行线程池最大线程
- `oasis.scheduler.worker.queue-capacity`: 执行队列大小
- `oasis.scheduler.callback.batch-size`: 回调批次大小
- `oasis.scheduler.callback.flush-interval-ms`: 回调刷新周期
- `oasis.scheduler.security.sign-algorithm`: 签名算法
- `oasis.scheduler.security.clock-skew-ms`: 时间偏移容忍

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
    heartbeat:
      interval-ms: 2000
      miss-threshold: 2
```
