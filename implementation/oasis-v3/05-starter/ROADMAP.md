# ROADMAP

## 一期
- 内置 Netty HTTP Server（`/invoke`）
- 注册/心跳/执行/回调闭环
- 3 秒下线感知（心跳 + 主动探测）

## 二期
- Netty（或 gRPC streaming）长连接通道
- Admin 与客户端双通道（长连接主通道，Netty HTTP 回退）
