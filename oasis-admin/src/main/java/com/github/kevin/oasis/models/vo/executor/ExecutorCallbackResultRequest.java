package com.github.kevin.oasis.models.vo.executor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 执行结果回调请求
 */
@Data
public class ExecutorCallbackResultRequest {

    @NotNull(message = "日志ID不能为空")
    private Long fireLogId;

    private Integer attemptNo;

    @NotBlank(message = "状态不能为空")
    private String status;

    private String errorMessage;

    private String executorAddress;

    private Long finishTime;
}
