package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorInvokeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询路由策略。
 */
@Component
@RequiredArgsConstructor
public class RoundRouteDispatchStrategy implements RouteDispatchStrategy {

    private final ExecutorInvokeClient executorInvokeClient;

    private final ConcurrentHashMap<Long, AtomicInteger> roundCursor = new ConcurrentHashMap<>();

    @Override
    public String route() {
        return "ROUND";
    }

    @Override
    public DispatchResult dispatch(RouteDispatchContext context) {
        AtomicInteger cursor = roundCursor.computeIfAbsent(context.getJobId(), key -> new AtomicInteger(0));
        int idx = Math.floorMod(cursor.getAndIncrement(), context.getNodes().size());
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
