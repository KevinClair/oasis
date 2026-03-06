package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务告警策略
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAlarmPolicyVO {

    private Long id;

    private Long jobId;

    private Boolean inheritAppTemplate;

    private List<String> receivers;

    private List<String> channels;

    private Integer quietPeriodMinutes;

    private Integer failThreshold;

    private Integer timeoutSeconds;

    private Boolean enabled;
}
