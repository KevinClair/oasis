package com.github.kevin.oasis.models.vo.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存任务请求
 */
@Data
public class JobSaveRequest {

    private Long id;

    @NotBlank(message = "应用编码不能为空")
    private String appCode;

    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    @NotBlank(message = "Handler不能为空")
    private String handlerName;

    @NotBlank(message = "调度类型不能为空")
    private String scheduleType;

    @NotBlank(message = "调度配置不能为空")
    private String scheduleConf;

    private String routeStrategy;

    private String blockStrategy;

    private Integer retryCount;

    private Integer timeoutSeconds;

    @NotNull(message = "状态不能为空")
    private Boolean status;

    private Boolean alarmInheritApp;
}
