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
 * Execute invoke requests in worker pool.
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

        OasisJobContext context = new OasisJobContext(request.getFireLogId(), request.getAttemptNo(), request.getTriggerParam());

        try {
            OasisJobHandler handler = handlerRegistry.get(request.getHandlerName())
                    .orElseThrow(() -> new IllegalStateException("handler not found: " + request.getHandlerName()));

            OasisJobResult result = handler.execute(context);
            if (!result.isSuccess()) {
                status = result.isRetryable() ? "FAILED" : "FAILED";
                errMsg = result.getMessage();
            }
        } catch (Exception e) {
            status = "FAILED";
            errMsg = e.getMessage();
            context.log("exception: " + e.getMessage());
            log.error("execute handler failed, fireLogId={}, handler={}", request.getFireLogId(), request.getHandlerName(), e);
        } finally {
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
