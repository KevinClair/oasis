package com.github.kevin.oasis.models.vo.executor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 执行日志回调请求
 */
@Data
public class ExecutorCallbackLogRequest {

    @NotNull(message = "日志ID不能为空")
    private Long fireLogId;

    @NotNull(message = "日志序号不能为空")
    private Integer seqNo;

    @NotNull(message = "日志时间不能为空")
    private Long logTime;

    @NotBlank(message = "日志内容不能为空")
    private String logContent;
}
