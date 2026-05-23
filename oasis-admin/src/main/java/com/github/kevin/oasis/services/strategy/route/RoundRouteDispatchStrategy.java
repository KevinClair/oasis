package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorInvokeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 轮询路由策略。
 */
@Component
@RequiredArgsConstructor
public class RoundRouteDispatchStrategy implements RouteDispatchStrategy {

    private final ExecutorInvokeClient executorInvokeClient;

    /**
     * 全局递增计数器，跨所有 job 共享，避免 per-job ConcurrentHashMap 无界增长导致内存泄漏。
     * 轮询均衡效果与 per-job 计数器一致，且不随时间推移退化。
     */
    private final AtomicLong globalCursor = new AtomicLong(0);

    @Override
    public String route() {
        return "ROUND";
    }

    @Override
    public DispatchResult dispatch(RouteDispatchContext context) {
        int idx = Math.floorMod(globalCursor.getAndIncrement(), context.getNodes().size());
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
