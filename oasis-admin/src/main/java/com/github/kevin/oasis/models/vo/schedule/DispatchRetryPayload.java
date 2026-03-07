package com.github.kevin.oasis.models.vo.schedule;

import lombok.Builder;
import lombok.Data;

/**
 * dispatch_queue 中 payload 的结构化内容。
 */
@Data
@Builder
public class DispatchRetryPayload {

    private String triggerParam;

    /**
     * 可重试次数（不含首次下发）。
     */
    private Integer maxRetry;

    /**
     * 最近一次失败原因，便于排障。
     */
    private String lastError;
}
