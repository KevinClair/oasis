package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务级告警策略
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAlarmPolicy {

    private Long id;

    private Long jobId;

    private Boolean inheritAppTemplate;

    /**
     * JSON string
     */
    private String receivers;

    /**
     * JSON string
     */
    private String channels;

    private Integer quietPeriodMinutes;

    private Integer failThreshold;

    private Integer timeoutSeconds;

    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
