package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 应用告警模板
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppAlarmTemplateVO {

    private Long id;

    private String appCode;

    private List<String> receivers;

    private List<String> channels;

    private Integer quietPeriodMinutes;

    private Integer failThreshold;

    private Integer timeoutSeconds;

    private Boolean enabled;
}
