# STARTER_DESIGN_V3

## 模块名
`oasis-scheduler-spring-boot-starter`

## 职责
- 客户端注册与心跳
- 接收调度请求并执行任务
- 回调执行结果与日志
- 优雅停机（drain）

## 接入前置
- `appCode` 必须先在 Admin 创建并启用。
- `appKey` 必须与 Admin 保存值匹配。

## 任务执行模型
- 业务实现 `OasisJobHandler` 接口。
- Starter 自动收集 Spring Bean 并建立 `handlerName -> handler` 映射。
- 调度请求到达后，投递线程池执行并回调结果。
