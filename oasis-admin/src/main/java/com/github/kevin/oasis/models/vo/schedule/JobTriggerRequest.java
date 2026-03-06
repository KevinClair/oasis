package com.github.kevin.oasis.models.vo.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务手动触发请求
 */
@Data
public class JobTriggerRequest {

    @NotNull(message = "任务ID不能为空")
    private Long jobId;

    private String triggerParam;
}
