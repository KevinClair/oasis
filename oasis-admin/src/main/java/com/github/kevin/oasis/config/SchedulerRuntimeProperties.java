package com.github.kevin.oasis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调度运行期配置
 */
@Data
@ConfigurationProperties(prefix = "oasis.scheduler.runtime")
public class SchedulerRuntimeProperties {

    /**
     * 是否启用 admin 调度扫描
     */
    private boolean enabled = true;

    /**
     * 调度扫描间隔（毫秒）
     */
    private long scanIntervalMs = 1000L;

    /**
     * 每次扫描最多捞取多少任务
     */
    private int scanLimit = 100;

    /**
     * 是否启用独立时间轮调度组件。
     * 关闭后退化为“分片扫描 + CAS 抢占”的兜底模式。
     */
    private boolean timeWheelEnabled = true;

    /**
     * 时间轮 tick 间隔（毫秒）。
     */
    private long timeWheelTickMs = 1000L;

    /**
     * 时间轮槽位数量。
     */
    private int timeWheelSlotCount = 120;

    /**
     * 时间轮预加载间隔（毫秒）。
     */
    private long timeWheelPreloadIntervalMs = 1000L;

    /**
     * 预加载窗口（毫秒）：仅将未来窗口内任务放入时间轮。
     */
    private long timeWheelPreloadWindowMs = 60_000L;

    /**
     * 预加载批次大小。
     */
    private int timeWheelPreloadBatchSize = 2000;

    /**
     * 调度分片总数（需与 job_schedule.shard_id 取值一致）
     */
    private int shardCount = 128;

    /**
     * 是否启用 shard_lease 租约调度。
     * 关闭后退化为“全节点扫描 + CAS 抢占”。
     */
    private boolean shardLeaseEnabled = true;

    /**
     * 租约续租/分配间隔（毫秒）。
     */
    private long shardLeaseRenewIntervalMs = 1000L;

    /**
     * 租约有效期（毫秒）。
     */
    private long shardLeaseDurationMs = 5000L;

    /**
     * scheduler_node 心跳间隔（毫秒）。
     */
    private long schedulerNodeHeartbeatIntervalMs = 1000L;

    /**
     * 调度节点心跳超时阈值（毫秒）。
     */
    private long schedulerNodeHeartbeatTimeoutMs = 5000L;

    /**
     * 调度节点对外端口（仅写入 scheduler_node 表用于可观测性）。
     */
    private int schedulerNodePort = 8080;

    /**
     * 可选固定节点ID。
     * 未配置时默认使用 host:port，避免每次重启生成新节点记录。
     */
    private String schedulerNodeId;

    /**
     * 执行器心跳超时阈值（毫秒）
     */
    private long executorHeartbeatTimeoutMs = 10_000L;

    /**
     * 执行器离线检查间隔（毫秒）
     */
    private long executorOfflineCheckIntervalMs = 5000L;

    /**
     * 执行器下发连接超时（毫秒）
     */
    private int invokeConnectTimeoutMs = 1000;

    /**
     * 执行器下发读超时（毫秒）
     */
    private int invokeReadTimeoutMs = 5000;

    /**
     * 执行器默认 context path
     */
    private String executorContextPath = "/oasis-executor";

    /**
     * 是否开启执行器接口 HMAC 鉴权
     */
    private boolean executorAuthEnabled = true;

    /**
     * 执行器请求时间偏移容忍值（毫秒）
     */
    private long executorAuthClockSkewMs = 30_000L;

    /**
     * nonce 过期时间（毫秒）
     */
    private long executorAuthNonceExpireMs = 120_000L;

    /**
     * nonce 本地缓存上限
     */
    private int executorAuthNonceMaxSize = 20_000;

    /**
     * dispatch_queue 异步重试开关
     */
    private boolean dispatchRetryEnabled = true;

    /**
     * dispatch_queue 扫描间隔（毫秒）
     */
    private long dispatchRetryIntervalMs = 1000L;

    /**
     * 每次最多处理多少条重试任务
     */
    private int dispatchRetryBatchSize = 100;

    /**
     * 重试基础退避时长（毫秒）
     */
    private long dispatchRetryBackoffMs = 3000L;

    /**
     * 重试最大退避时长（毫秒）
     */
    private long dispatchRetryMaxBackoffMs = 60000L;
}
