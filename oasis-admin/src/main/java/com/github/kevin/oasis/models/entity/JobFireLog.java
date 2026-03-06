package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调度执行日志主表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobFireLog {

    private Long id;

    private Long jobId;

    private Long triggerTime;

    private Long finishTime;

    private String triggerType;

    private String executorAddress;

    private String status;

    private Integer attemptNo;

    private String errorMessage;

    private String traceId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
