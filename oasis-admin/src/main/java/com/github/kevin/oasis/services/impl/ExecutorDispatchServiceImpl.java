package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.ExecutorNodeDao;
import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.vo.executor.ExecutorInvokeRequest;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 执行器路由和下发实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutorDispatchServiceImpl implements ExecutorDispatchService {

    private final ExecutorNodeDao executorNodeDao;
    private final SchedulerRuntimeProperties runtimeProperties;

    private final ConcurrentHashMap<Long, AtomicInteger> roundCursor = new ConcurrentHashMap<>();

    private volatile RestTemplate restTemplate;

    @Override
    public DispatchResult dispatch(JobInfo jobInfo, Long fireLogId, Integer attemptNo, String triggerParam) {
        long now = System.currentTimeMillis();
        long heartbeatAfter = now - runtimeProperties.getExecutorHeartbeatTimeoutMs();
        List<ExecutorNode> nodes = executorNodeDao.selectOnlineByAppCode(jobInfo.getAppCode(), heartbeatAfter);
        if (nodes == null || nodes.isEmpty()) {
            return DispatchResult.builder()
                    .success(false)
                    .errorMessage("无可用执行器节点")
                    .build();
        }

        String route = normalizeRoute(jobInfo.getRouteStrategy());
        return switch (route) {
            case "RANDOM" -> dispatchRandom(nodes, fireLogId, attemptNo, jobInfo.getHandlerName(), triggerParam);
            case "FAILOVER" -> dispatchFailover(nodes, fireLogId, attemptNo, jobInfo.getHandlerName(), triggerParam);
            case "BROADCAST" -> dispatchBroadcastFallback(nodes, fireLogId, attemptNo, jobInfo.getHandlerName(), triggerParam);
            default -> dispatchRound(jobInfo.getId(), nodes, fireLogId, attemptNo, jobInfo.getHandlerName(), triggerParam);
        };
    }

    private DispatchResult dispatchRound(Long jobId, List<ExecutorNode> nodes, Long fireLogId, Integer attemptNo,
                                         String handlerName, String triggerParam) {
        AtomicInteger cursor = roundCursor.computeIfAbsent(jobId, key -> new AtomicInteger(0));
        int idx = Math.floorMod(cursor.getAndIncrement(), nodes.size());
        ExecutorNode node = nodes.get(idx);
        return invoke(node.getAddress(), fireLogId, attemptNo, handlerName, triggerParam);
    }

    private DispatchResult dispatchRandom(List<ExecutorNode> nodes, Long fireLogId, Integer attemptNo,
                                          String handlerName, String triggerParam) {
        int idx = ThreadLocalRandom.current().nextInt(nodes.size());
        ExecutorNode node = nodes.get(idx);
        return invoke(node.getAddress(), fireLogId, attemptNo, handlerName, triggerParam);
    }

    private DispatchResult dispatchFailover(List<ExecutorNode> nodes, Long fireLogId, Integer attemptNo,
                                            String handlerName, String triggerParam) {
        StringBuilder errors = new StringBuilder();
        for (ExecutorNode node : nodes) {
            DispatchResult result = invoke(node.getAddress(), fireLogId, attemptNo, handlerName, triggerParam);
            if (result.isSuccess()) {
                return result;
            }
            if (errors.length() > 0) {
                errors.append(" | ");
            }
            errors.append(node.getAddress()).append(":").append(result.getErrorMessage());
        }

        return DispatchResult.builder()
                .success(false)
                .errorMessage(errors.toString())
                .build();
    }

    private DispatchResult dispatchBroadcastFallback(List<ExecutorNode> nodes, Long fireLogId, Integer attemptNo,
                                                     String handlerName, String triggerParam) {
        // 一期单 fireLog 模型下，广播到多节点会导致回调和日志覆盖冲突，先降级为单节点下发。
        ExecutorNode node = nodes.get(0);
        DispatchResult result = invoke(node.getAddress(), fireLogId, attemptNo, handlerName, triggerParam);
        if (!result.isSuccess()) {
            return result;
        }
        result.setErrorMessage("BROADCAST在一期降级为单节点执行");
        return result;
    }

    private DispatchResult invoke(String address, Long fireLogId, Integer attemptNo, String handlerName, String triggerParam) {
        try {
            ExecutorInvokeRequest request = ExecutorInvokeRequest.builder()
                    .fireLogId(fireLogId)
                    .attemptNo(attemptNo == null ? 1 : attemptNo)
                    .handlerName(handlerName)
                    .triggerParam(triggerParam)
                    .build();

            String url = "http://" + address + normalizeContextPath(runtimeProperties.getExecutorContextPath()) + "/invoke";
            ResponseEntity<Map> response = getRestTemplate().postForEntity(url, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return DispatchResult.builder()
                        .success(false)
                        .errorMessage("HTTP状态码=" + response.getStatusCode().value())
                        .build();
            }

            Object body = response.getBody();
            if (body instanceof Map<?, ?> map) {
                Object accepted = map.get("accepted");
                if (Boolean.TRUE.equals(accepted)) {
                    return DispatchResult.builder()
                            .success(true)
                            .executorAddress(address)
                            .build();
                }

                return DispatchResult.builder()
                        .success(false)
                        .errorMessage("执行器拒绝接收")
                        .build();
            }

            return DispatchResult.builder()
                    .success(false)
                    .errorMessage("执行器响应格式异常")
                    .build();
        } catch (Exception e) {
            log.warn("invoke executor failed, address={}, fireLogId={}, msg={}", address, fireLogId, e.getMessage());
            return DispatchResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate != null) {
            return restTemplate;
        }

        synchronized (this) {
            if (restTemplate == null) {
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(runtimeProperties.getInvokeConnectTimeoutMs());
                requestFactory.setReadTimeout(runtimeProperties.getInvokeReadTimeoutMs());
                restTemplate = new RestTemplate(requestFactory);
            }
        }

        return restTemplate;
    }

    private String normalizeContextPath(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String path = value.startsWith("/") ? value : "/" + value;
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private String normalizeRoute(String route) {
        if (route == null || route.isBlank()) {
            return "ROUND";
        }
        return route.trim().toUpperCase(Locale.ROOT);
    }
}
