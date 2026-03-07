# oasis-scheduler-spring-boot-starter

Spring Boot Starter for Oasis scheduler executor clients.

## Core features
- Node register / heartbeat
- Built-in Netty HTTP invoke endpoint
- Handler registry (interface-based)
- Result/log callbacks
- Graceful shutdown of worker pool

## Basic config
```yaml
oasis:
  scheduler:
    enabled: true
    app-code: order-service
    app-key: your-app-key
    admin:
      base-url: http://127.0.0.1:8080
    server:
      port: 19091
      context-path: /oasis-executor
      verify-invoke-signature: true
```
