package com.github.kevin.oasis.models.vo.executor;

import lombok.Builder;
import lombok.Data;

/**
 * 下发执行请求（admin -> executor）
 */
@Data
@Builder
public class ExecutorInvokeRequest {

    private Long fireLogId;

    private Integer attemptNo;

    private String handlerName;

    private String triggerParam;
}
