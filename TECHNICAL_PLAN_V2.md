# 分布式调度平台 - 技术方案 (V2)

## 1. 概述

本技术方案旨在设计一个自主可控、中心化的分布式任务调度平台，其设计思想借鉴了 `xxl-job` 的成熟模式，但核心组件将进行自研实现。方案将深度集成到 `Oasis` 项目技术栈 (`Spring Boot` + `Vue 3`)，构建一个高可用、可扩展的调度系统。

## 2. 架构设计

平台遵循中心化的设计思想，由 **调度中心 (Scheduler-Admin)**、**执行器 (Executor-Client)** 和 **持久化存储** 三大核心部分组成。

```mermaid
graph TD
    subgraph "用户界面"
        A[开发者/运维] --> B(Oasis Web UI)
    end

    subgraph "调度中心 (Oasis Admin - 中心化大脑)"
        B -- "1. 管理操作 (REST API)" --> C{API网关/控制器}
        C -- "2. 任务定义/修改" --> D[任务管理器]
        D -- "3. 更新调度计划" --> E{调度核心 (基于 Quartz)}
        E -- "4. 任务到点触发" --> F[调度任务处理器]
        F -- "6. 选择节点" --> G[执行器注册中心]
        F -- "7. 远程调度 (RPC)" --> H[执行器集群]
        C -- "CRUD" --> I[(MySQL数据库)]
        G -- "5. 获取可用节点" --> I
    end

    subgraph "执行器 (业务应用)"
        H -- "心跳/注册" --> G
        H -- "8. 接收并执行任务" --> J[任务执行单元 (Handler)]
        J -- "9. 上报日志/结果" --> C
    end

    style B fill:#f9f,stroke:#333,stroke-width:2px
    style C fill:#ccf,stroke:#333,stroke-width:2px
    style H fill:#cfc,stroke:#333,stroke-width:2px
```

### 2.1. 核心组件

- **调度中心 (Scheduler-Admin)**:
    - **技术栈**: `Spring Boot` (集成到 `oasis-admin`) + `Quartz` (作为底层调度引擎)。
    - **职责**:
        - **API服务**: 为前端 `oasis-web` 提供标准的RESTful API，用于应用、任务、日志的增删改查。
        - **任务管理**: 维护任务元数据，并将其动态地转化为 `Quartz` 的 `Job` 和 `Trigger`。是平台与底层调度引擎的解耦层。
        - **调度核心**: 基于 `Quartz` 的JDBC持久化和集群能力，实现调度任务的高可用存储和触发。调度中心自身可以水平扩展。
        - **注册中心**: 维护执行器应用和节点的在线列表，处理客户端的注册和心跳，实现服务��现和故障摘除。
        - **任务分发**: 在任务被 `Quartz` 触发后，根据路由策略选择一个或多个目标节点，并发起远程调用。
- **执行器客户端 (Executor-Client)**:
    - **技术栈**: 以一个自定义的 `oasis-scheduler-client` Spring Boot Starter 的形式提供。
    - **职责**:
        - **服务注册与心跳**: 客户端启动时，自动向调度中心注册，并由后台线程维持心跳。
        - **任务接收器**: 内置一个轻量级的RPC服务端（如基于 Netty 或内嵌 Tomcat 的 `Controller`），用于接收调度中心的调度请求。
        - **任务执行**: 维护一个本地的任务方法注册表（`Handler`），根据调度请求动态调用到具体的业务逻辑。
        - **上下文与日志**: 提供 `OasisJobContext` 之类的工具，允许业务代码获取任务参数，并向调度中心上报执行日志。
- **数据库**:
    - **技术栈**: `MySQL`。
    - **职责**:
        - 持久化存储**业务调度数据**：应用信息、任务定义、调度日志等。
        - 持久化存储**Quartz调度数据**：`QRTZ_` 系列表，用于存储 `Job`、`Trigger` 和集群状态。

## 3. 核心流程详解

### 3.1. 一次调度任务的完整生命周期

1.  **任务创建与注��**: 用户通过UI创建一个CRON任务。`任务管理器` 将其保存到业务表，并调用 `Quartz` API 创建一个 `JobDetail` 和一个 `CronTrigger`，存储到 `QRTZ_` 表中。
2.  **调度触发 (中心化)**: `Quartz` 调度器集群中的某个 `Active` 节点，其工作线程会扫描数据库，发现该 `Trigger` 已到触发时间。
3.  **任务预处理**: `Quartz` 触发对应的 `Job`（我们自定义的一个通用 `RemoteDispatchJob`）。该 `Job` 的 `execute` 方法被调用，方法体内获取到任务ID。
4.  **获取执行器列表**: `RemoteDispatchJob` 调用 `执行器注册中心` 服务，根据任务配置的 `AppName`，获取当前所有健康的执行器节点地址列表。
5.  **路由与分发**: 根据任务配置的**路由策略**（如：轮询），从节点列表中选择一个目标节点。
6.  **发起RPC调用**: 调度中心通过 `HTTP` (或 `gRPC`) 向目标节点的 `RPC任务接收器` 发起调用。
    - **请求体 (Payload)**: 包含 `handler` (任务标识)、`params` (任务参数)、`logId` (调度日志ID)、`timeout` (超时时间) 等关键信息。
7.  **执行器接收与执行**:
    - 执行器端的 `任务接收器` 收到请求。
    - 根据 `handler` 从本地 `任务方法注册表` 中找到对应的方法。
    - 通过反射调用���方法，并将 `params` 等上下文信息传递给业务逻辑。
8.  **日志与结果上报**:
    - **执行日志**: 业务代码中通过 `OasisJobContext.log("...")` 打印的日志，被异步批量地通过 `HTTP` 回调发送给调度中心。
    - **执行结果**: 任务执行完毕后，执行器将最终结果（成功/失败、耗时、错误信息）通过 `HTTP` 回调接口报告给调度中心。
9.  **调度闭环**: 调度中心接收到执行结果，更新调度日志的状态。如果配置了失败重试或任务依赖，则触发相应后续逻辑。

## 4. 技术细节与实现方案

### 4.1. `oasis-admin` (调度中心) 核心实现

1.  **引入依赖**: 在 `oasis-admin` 的 `pom.xml` 中添加 `spring-boot-starter-quartz`。
2.  **配置 `application.properties`**:
    ```properties
    # Quartz 使用业务数据源
    spring.quartz.job-store-type=jdbc
    # Quartz 集群配置
    spring.quartz.properties.org.quartz.scheduler.instanceId=AUTO
    spring.quartz.properties.org.quartz.jobStore.isClustered=true
    spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval=5000
    ```
3.  **数据库初始化**: 除了业务表，还需要使用 `Quartz` 官方提供的 `tables_mysql_innodb.sql` 脚本初始化 `QRTZ_` 系列表。
4.  **核心服务**:
    - **`JobService`**: 封装对 `Quartz` API 的操作，实现 `addJob`, `updateJob`, `pauseJob` 等业务接口。将业务任务模型与 `Quartz` 的 `JobDetail/Trigger` 进行转换。
    - **`RegistryService`**: 内存+数据库实现执行器注册表。提供 `registry`, `heartbeat`, `discover` 等接口。使用 `ConcurrentHashMap` 存储在线节点，并定期与数据库同步。
    - **`RemoteDispatchJob`**: 一个实现了 `org.quartz.Job` 接口的类，是所有调度任务在 `Quartz` 中的统一入口点。

### 4.2. `oasis-scheduler-client` (执行器客户端Starter)

这是需要自研的关键模块。

1.  **`@OasisJob` 注解**:
    ```java
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface OasisJob {
        String value(); // 任务的唯一标识 (Handler)
    }
    ```
2.  **`BeanPostProcessor`**: 实现一个 `BeanPostProcessor`，在Spring容器初始化后，扫描所有被 `@OasisJob` 注解的方法，将其 `bean` 和 `method` 缓存到一个 `Map<String, JobHandler>` 中。
3.  **`JobInvokeController`**: 一个内置的 `RestController`，提供一个唯一的入口点，如 `POST /oasis-scheduler/invoke`。它接收调度中心的RPC请求，从缓存的 `Map` 中找到 `JobHandler` 并执行。
4.  **`HeartbeatWorker`**: 一个后台线程/定时任务，在客户端启动后，定期向调度中心的 `/api/registry/heartbeat` 接口发送心跳。
5.  **`LogAppender`**: 可以通过 `Logback/Log4j2` 的自定义 `Appender` 实现，将带有特定标记的日志异步发送回调度中心。

### 4.3. 通信协议

- **推荐使用 HTTP**: 简单、通用、易于调试。使用 `RestTemplate` 或 `FeignClient` 即可实现。
- **调度中心 -> 执行器**: `POST /invoke`
- **执行器 -> 调度中心**:
    - `POST /registry/register` (注册)
    - `POST /registry/heartbeat` (心跳)
    - `POST /log/callback` (日志上报)
    - `POST /job/callback` (结果回调)

所有通信都应携带 `AccessToken` 进行简单的安全认证。

## 5. 后续工作规划

1.  **数据库表设计**: 设计 `oasis_job_app` (应用), `oasis_job_info` (任务), `oasis_job_log` (日志) 等表结构。
2.  **Client-Starter 开发**: 优先开发 `oasis-scheduler-client` 模块，定义注解和核心通信逻辑。
3.  **调度中心开发**: 在 `oasis-admin` 中集成 `Quartz` 并开发应用管理、任务管理和日志服务。
4.  **前端页面开发**: 在 `oasis-web` 中根据产品方案开发对应的管理界面。
5.  **端到端联调**: 部署并测试一个完整的任务创建->调度->执行->回调流程。

