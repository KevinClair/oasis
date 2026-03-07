package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorInvokeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机路由策略。
 */
@Component
@RequiredArgsConstructor
public class RandomRouteDispatchStrategy implements RouteDispatchStrategy {

    private final ExecutorInvokeClient executorInvokeClient;

    @Override
    public String route() {
        return "RANDOM";
    }

    @Override
    public DispatchResult dispatch(RouteDispatchContext context) {
        int idx = ThreadLocalRandom.current().nextInt(context.getNodes().size());
        ExecutorNode node = context.getNodes().get(idx);
        return executorInvokeClient.invoke(
                node.getAddress(),
                context.getApp(),
                context.getFireLogId(),
                context.getAttemptNo(),
                context.getHandlerName(),
                context.getTriggerParam()
        );
    }
}
