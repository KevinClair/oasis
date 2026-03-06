# INTEGRATION_GUIDE

## 接入步骤
1. 引入 `oasis-scheduler-spring-boot-starter` 依赖。
2. 配置 `oasis.scheduler.*` 参数。
3. 实现 `OasisJobHandler` 并注册为 Spring Bean。
4. 启动应用，观察注册与心跳日志。
5. 在 Admin 控制台创建任务并绑定 handler。

## 注意事项
- 未在 Admin 创建的 appCode 无法注册。
- 任务一期仅允许在控制台创建。
