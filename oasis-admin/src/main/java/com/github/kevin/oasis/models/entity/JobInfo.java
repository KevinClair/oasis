package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调度任务定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobInfo {

    private Long id;

    private String appCode;

    private String jobName;

    private String handlerName;

    private String scheduleType;

    private String scheduleConf;

    private String routeStrategy;

    private String blockStrategy;

    private Integer retryCount;

    private Integer timeoutSeconds;

    private Boolean status;

    private Boolean alarmInheritApp;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
