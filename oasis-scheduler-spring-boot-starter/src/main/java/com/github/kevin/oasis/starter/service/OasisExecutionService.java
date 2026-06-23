package com.github.kevin.oasis.starter.service;

import com.github.kevin.oasis.starter.autoconfigure.OasisSchedulerProperties;
import com.github.kevin.oasis.starter.client.OasisAdminClient;
import com.github.kevin.oasis.starter.context.OasisJobContext;
import com.github.kevin.oasis.starter.handler.OasisJobHandler;
import com.github.kevin.oasis.starter.model.*;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.*;

/**
 * 执行调用请求的线程池服务，支持超时中断。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OasisExecutionService {

    private final JobHandlerRegistry handlerRegistry;
    private final OasisAdminClient oasisAdminClient;
    private final OasisSchedulerProperties properties;

    private ExecutorService worker;

    private synchronized ExecutorService worker() {
        if (worker == null) {
            worker = new ThreadPoolExecutor(
                    properties.getWorker().getCoreSize(),
                    properties.getWorker().getMaxSize(),
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(properties.getWorker().getQueueCapacity()),
                    r -> {
                        Thread t = new Thread(r);
                        t.setName("oasis-worker-" + t.getId());
                        return t;
                    }
            );
        }
        return worker;
    }

    public boolean submit(ExecutorInvokeRequest request) {
        try {
            worker().submit(() -> execute(request));
            return true;
        } catch (Exception e) {
            log.error("submit invoke task failed, fireLogId={}", request.getFireLogId(), e);
            return false;
        }
    }

    private void execute(ExecutorInvokeRequest request) {
        String status = "SUCCESS";
        String errMsg = null;

        OasisJobContext context = new OasisJobContext(
                request.getFireLogId(), request.getAttemptNo(), request.getTriggerParam());

        OasisJobHandler handler = null;
        try {
            handler = handlerRegistry.get(request.getHandlerName())
                    .orElseThrow(() -> new IllegalStateException("handler not found: " + request.getHandlerName()));
        } catch (Exception e) {
            status = "FAILED";
            errMsg = e.getMessage();
            log.error("handler lookup failed, fireLogId={}, handler={}",
                    request.getFireLogId(), request.getHandlerName(), e);
            doCallback(request, context, status, errMsg);
            return;
        }

        int timeoutSec = request.getTimeoutSeconds() != null && request.getTimeoutSeconds() > 0
                ? request.getTimeoutSeconds() : 30;

        final OasisJobHandler finalHandler = handler;
        Future<?> future = worker().submit(() -> {
            try {
                OasisJobResult result = finalHandler.execute(context);
                return result;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return OasisJobResult.builder().success(false).retryable(false)
                        .message("执行超时被中断").build();
            } catch (Exception e) {
                return OasisJobResult.builder().success(false).retryable(false)
                        .message(e.getMessage()).build();
            }
        });

        try {
            OasisJobResult result = (OasisJobResult) future.get(timeoutSec, TimeUnit.SECONDS);
            if (!result.isSuccess()) {
                status = "FAILED";
                errMsg = result.getMessage();
            }
        } catch (TimeoutException e) {
            future.cancel(true);
            status = "TIMEOUT";
            errMsg = "执行超时，超过 " + timeoutSec + " 秒";
            context.log("TIMEOUT: " + errMsg);
            log.warn("handler execution timed out, fireLogId={}, handler={}, timeout={}s",
                    request.getFireLogId(), request.getHandlerName(), timeoutSec);
        } catch (Exception e) {
            status = "FAILED";
            errMsg = e.getMessage();
            context.log("exception: " + e.getMessage());
            log.error("execute handler failed, fireLogId={}, handler={}",
                    request.getFireLogId(), request.getHandlerName(), e);
        }

        doCallback(request, context, status, errMsg);
    }

    private void doCallback(ExecutorInvokeRequest request, OasisJobContext context, String status, String errMsg) {
        int seq = 1;
        for (OasisJobContext.LogLine line : context.getLogs()) {
            oasisAdminClient.callbackLog(ExecutorCallbackLogRequest.builder()
                    .fireLogId(request.getFireLogId())
                    .seqNo(seq++)
                    .logTime(line.time())
                    .logContent(line.content())
                    .build());
        }

        oasisAdminClient.callbackResult(ExecutorCallbackResultRequest.builder()
                .fireLogId(request.getFireLogId())
                .attemptNo(request.getAttemptNo())
                .status(status)
                .errorMessage(errMsg)
                .executorAddress(localAddress())
                .finishTime(System.currentTimeMillis())
                .build());
    }

    private String localAddress() {
        return hostname() + ":" + properties.getServer().getPort();
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    @PreDestroy
    public void shutdown() {
        if (worker != null) {
            worker.shutdown();
        }
    }
}
