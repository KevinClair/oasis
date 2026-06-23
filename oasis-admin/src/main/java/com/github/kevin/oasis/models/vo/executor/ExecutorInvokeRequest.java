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

    /** 执行超时时间（秒），executor 侧据此在 Future.get() 上加超时 */
    private Integer timeoutSeconds;
}
