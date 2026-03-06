package com.github.kevin.oasis.models.vo.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 保存任务告警策略请求
 */
@Data
public class JobAlarmPolicySaveRequest {

    @NotNull(message = "任务ID不能为空")
    private Long jobId;

    private Boolean inheritAppTemplate;

    private List<String> receivers;

    private List<String> channels;

    private Integer quietPeriodMinutes;

    private Integer failThreshold;

    private Integer timeoutSeconds;

    private Boolean enabled;
}
