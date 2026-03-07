package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 下发重试队列。
 * 用于保存下发失败的任务，并由异步重试线程补偿执行。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DispatchQueue {

    private Long id;

    private Long fireLogId;

    private Long jobId;

    private String targetAddress;

    private String payload;

    /**
     * PENDING / PROCESSING / SUCCESS / DEAD
     */
    private String status;

    private Integer retryCount;

    private Long nextRetryTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
