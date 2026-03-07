# NETTY_EVOLUTION_V3

## 演进目标
在一期 Netty HTTP Server 基线下，引入 Netty 长连接通道提升下线感知与下发时效。

## 演进策略
1. 保留 Netty HTTP 作为回退。
2. 引入长连接注册与在线状态推送。
3. 下发请求优先走长连接。
4. 连接断开即时标记节点不可路由。
