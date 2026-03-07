package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorInvokeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 故障转移路由策略。
 */
@Component
@RequiredArgsConstructor
public class FailoverRouteDispatchStrategy implements RouteDispatchStrategy {

    private final ExecutorInvokeClient executorInvokeClient;

    @Override
    public String route() {
        return "FAILOVER";
    }

    @Override
    public DispatchResult dispatch(RouteDispatchContext context) {
        StringBuilder errors = new StringBuilder();
        for (ExecutorNode node : context.getNodes()) {
            DispatchResult result = executorInvokeClient.invoke(
                    node.getAddress(),
                    context.getApp(),
                    context.getFireLogId(),
                    context.getAttemptNo(),
                    context.getHandlerName(),
                    context.getTriggerParam()
            );
            if (result.isSuccess()) {
                return result;
            }

            if (errors.length() > 0) {
                errors.append(" | ");
            }
            errors.append(node.getAddress()).append(":").append(result.getErrorMessage());
        }

        return DispatchResult.builder().success(false).errorMessage(errors.toString()).build();
    }
}
