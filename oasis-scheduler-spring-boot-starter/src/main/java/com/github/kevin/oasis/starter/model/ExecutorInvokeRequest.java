package com.github.kevin.oasis.starter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下发执行请求（admin -> executor）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorInvokeRequest {

    private Long fireLogId;

    private Integer attemptNo;

    private String handlerName;

    private String triggerParam;

    /** 执行超时时间（秒） */
    private Integer timeoutSeconds;
}
