package com.github.kevin.oasis.models.vo.schedule;

import lombok.Builder;
import lombok.Data;

/**
 * dispatch_queue 监控概览。
 */
@Data
@Builder
public class DispatchQueueOverviewVO {

    private Long pendingCount;

    private Long processingCount;

    private Long successCount;

    private Long deadCount;

    private Long duePendingCount;
}
