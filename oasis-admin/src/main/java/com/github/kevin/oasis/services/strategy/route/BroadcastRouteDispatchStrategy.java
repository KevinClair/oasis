package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorInvokeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 广播路由策略：向所有在线执行器节点并行下发任务。
 * <p>
 * 典型场景：清空本地缓存、刷新配置等需要所有实例同时执行的 Handler。
 * 回调合并策略：多个执行器回调同一个 fireLog 时，由
 * ExecutorGatewayServiceImpl 按 attemptNo 做幂等处理。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BroadcastRouteDispatchStrategy implements RouteDispatchStrategy {

    private final ExecutorInvokeClient executorInvokeClient;

    @Override
    public String route() {
        return "BROADCAST";
    }

    @Override
    public DispatchResult dispatch(RouteDispatchContext context) {
        List<ExecutorNode> nodes = context.getNodes();
        if (nodes.isEmpty()) {
            return DispatchResult.builder()
                    .success(false)
                    .errorMessage("无可用执行器节点")
                    .build();
        }

        List<CompletableFuture<DispatchResult>> futures = new ArrayList<>(nodes.size());
        for (ExecutorNode node : nodes) {
            CompletableFuture<DispatchResult> future = CompletableFuture.supplyAsync(() ->
                    executorInvokeClient.invoke(
                            node.getAddress(),
                            context.getApp(),
                            context.getFireLogId(),
                            context.getAttemptNo(),
                            context.getHandlerName(),
                            context.getTriggerParam()
                    )
            );
            futures.add(future);
        }

        // 等待所有下发完成（单次下发超时由 ExecutorInvokeClient 内部控制）
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("broadcast wait interrupted, jobId={}", context.getJobId(), e);
        }

        // 聚合结果
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            try {
                DispatchResult result = futures.get(i).getNow(
                        DispatchResult.builder().success(false).errorMessage("超时").build()
                );
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    errors.add(nodes.get(i).getAddress() + ":" + result.getErrorMessage());
                }
            } catch (Exception e) {
                errors.add(nodes.get(i).getAddress() + ":异常-" + e.getMessage());
            }
        }

        boolean anySuccess = successCount > 0;
        String message = successCount + "/" + nodes.size() + " 节点接受";
        if (!errors.isEmpty()) {
            message += " | 失败: " + String.join(", ", errors);
        }

        return DispatchResult.builder()
                .success(anySuccess)
                .errorMessage(message)
                .executorAddress(nodes.get(0).getAddress()) // 取首个节点地址作为代表
                .build();
    }
}
