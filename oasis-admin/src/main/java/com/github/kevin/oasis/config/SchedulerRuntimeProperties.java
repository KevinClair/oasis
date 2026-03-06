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
}
