package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorInvokeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * BROADCAST 一期安全降级策略。
 * 由于当前 fireLog 是单记录模型，无法安全承载多节点并发回调，故降级为单节点执行。
 */
@Component
@RequiredArgsConstructor
public class BroadcastFallbackRouteDispatchStrategy implements RouteDispatchStrategy {

    private final ExecutorInvokeClient executorInvokeClient;

    @Override
    public String route() {
        return "BROADCAST";
    }

    @Override
    public DispatchResult dispatch(RouteDispatchContext context) {
        ExecutorNode node = context.getNodes().get(0);
        DispatchResult result = executorInvokeClient.invoke(
                node.getAddress(),
                context.getApp(),
                context.getFireLogId(),
                context.getAttemptNo(),
                context.getHandlerName(),
                context.getTriggerParam()
        );
        if (result.isSuccess()) {
            result.setErrorMessage("BROADCAST在一期降级为单节点执行");
        }
        return result;
    }
}
