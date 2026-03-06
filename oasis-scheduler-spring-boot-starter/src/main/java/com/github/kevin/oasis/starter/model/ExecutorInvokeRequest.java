package com.github.kevin.oasis.starter.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Invoke request from Oasis admin.
 */
@Data
public class ExecutorInvokeRequest {

    @NotNull
    private Long fireLogId;

    private Integer attemptNo = 1;

    @NotBlank
    private String handlerName;

    private String triggerParam;
}
