package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.ApplicationDao;
import com.github.kevin.oasis.dao.ExecutorNodeDao;
import com.github.kevin.oasis.models.entity.Application;
import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorDispatchService;
import com.github.kevin.oasis.services.strategy.route.RouteDispatchContext;
import com.github.kevin.oasis.services.strategy.route.RouteDispatchStrategyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 执行器路由和下发实现
 */
@Service
@RequiredArgsConstructor
public class ExecutorDispatchServiceImpl implements ExecutorDispatchService {

    private final ExecutorNodeDao executorNodeDao;
    private final ApplicationDao applicationDao;
    private final SchedulerRuntimeProperties runtimeProperties;
    private final RouteDispatchStrategyRegistry routeDispatchStrategyRegistry;

    @Override
    public DispatchResult dispatch(JobInfo jobInfo, Long fireLogId, Integer attemptNo, String triggerParam) {
        long now = System.currentTimeMillis();
        long heartbeatAfter = now - runtimeProperties.getExecutorHeartbeatTimeoutMs();
        // 仅路由到心跳窗口内的 ONLINE 节点。
        List<ExecutorNode> nodes = executorNodeDao.selectOnlineByAppCode(jobInfo.getAppCode(), heartbeatAfter);
        if (nodes == null || nodes.isEmpty()) {
            return DispatchResult.builder()
                    .success(false)
                    .errorMessage("无可用执行器节点")
                    .build();
        }

        Application app = applicationDao.selectByAppCode(jobInfo.getAppCode());
        if (app == null || app.getAppKey() == null || app.getAppKey().isBlank()) {
            return DispatchResult.builder()
                    .success(false)
                    .errorMessage("应用不存在或缺少密钥，无法签名下发")
                    .build();
        }

        RouteDispatchContext context = RouteDispatchContext.builder()
                .jobId(jobInfo.getId())
                .app(app)
                .nodes(nodes)
                .fireLogId(fireLogId)
                .attemptNo(attemptNo)
                .handlerName(jobInfo.getHandlerName())
                .triggerParam(triggerParam)
                .build();

        // 路由模式切换统一由 Spring 策略注册中心负责，避免 switch 分支膨胀。
        return routeDispatchStrategyRegistry.dispatch(jobInfo.getRouteStrategy(), context);
    }
}
