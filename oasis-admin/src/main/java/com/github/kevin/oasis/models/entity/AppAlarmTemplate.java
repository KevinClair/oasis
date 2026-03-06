package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 应用默认告警模板
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppAlarmTemplate {

    private Long id;

    private String appCode;

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
