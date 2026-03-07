package com.github.kevin.oasis.models.vo.schedule;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * dispatch_queue 列表项。
 */
@Data
@Builder
public class DispatchQueueVO {

    private Long id;

    private Long fireLogId;

    private Long jobId;

    private String targetAddress;

    private String status;

    private Integer retryCount;

    private Long nextRetryTime;

    private String payload;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
