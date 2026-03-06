package com.github.kevin.oasis.models.vo.schedule;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 任务启停请求
 */
@Data
public class JobEnableRequest {

    @NotEmpty(message = "任务ID列表不能为空")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    private Boolean status;
}
