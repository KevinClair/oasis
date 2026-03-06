package com.github.kevin.oasis.models.vo.schedule;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 保存应用告警模板请求
 */
@Data
public class AppAlarmTemplateSaveRequest {

    @NotBlank(message = "应用编码不能为空")
    private String appCode;

    private List<String> receivers;

    private List<String> channels;

    private Integer quietPeriodMinutes;

    private Integer failThreshold;

    private Integer timeoutSeconds;

    private Boolean enabled;
}
