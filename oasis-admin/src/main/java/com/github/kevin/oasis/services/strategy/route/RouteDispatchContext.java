package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.models.entity.Application;
import com.github.kevin.oasis.models.entity.ExecutorNode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 路由策略执行上下文。
 */
@Data
@Builder
public class RouteDispatchContext {

    private Long jobId;

    private Application app;

    private List<ExecutorNode> nodes;

    private Long fireLogId;

    private Integer attemptNo;

    private String handlerName;

    private String triggerParam;
}
