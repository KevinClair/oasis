package com.github.kevin.oasis.starter.service;

import com.github.kevin.oasis.starter.autoconfigure.OasisSchedulerProperties;
import com.github.kevin.oasis.starter.client.OasisAdminClient;
import com.github.kevin.oasis.starter.model.ExecutorHeartbeatRequest;
import com.github.kevin.oasis.starter.model.ExecutorRegisterRequest;
import com.github.kevin.oasis.starter.server.ExecutorNettyHttpServer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Register / heartbeat lifecycle.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OasisLifecycleService {

    private final OasisSchedulerProperties properties;
    private final OasisAdminClient oasisAdminClient;
    private final JobHandlerRegistry jobHandlerRegistry;
    private final ExecutorNettyHttpServer executorNettyHttpServer;

    private volatile boolean registered = false;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        jobHandlerRegistry.init();

        // 先启动接收端，再向 admin 注册，确保注册后即可被下发调度请求。
        boolean serverStarted = executorNettyHttpServer.start();
        if (!serverStarted) {
            log.error("oasis netty server not started, skip register");
            return;
        }
        registered = doRegister();
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.heartbeat.interval-ms:2000}")
    public void heartbeat() {
        if (!properties.isEnabled()) return;

        if (!registered) {
            registered = doRegister();
            return;
        }

        ExecutorHeartbeatRequest request = ExecutorHeartbeatRequest.builder()
                .appCode(properties.getAppCode())
                .appKey(properties.getAppKey())
                .address(localAddress())
                .metaJson("{}")
                .build();

        boolean ok = oasisAdminClient.heartbeat(request);
        if (!ok) {
            log.warn("oasis heartbeat failed");
        }
    }

    @PreDestroy
    public void onShutdown() {
        log.info("oasis starter shutdown, stop heartbeat");
        registered = false;
        executorNettyHttpServer.stop();
    }

    private boolean doRegister() {
        ExecutorRegisterRequest request = ExecutorRegisterRequest.builder()
                .appCode(properties.getAppCode())
                .appKey(properties.getAppKey())
                .address(localAddress())
                .machineTag(hostname())
                .metaJson("{}")
                .build();
        boolean ok = oasisAdminClient.register(request);
        if (ok) {
            log.info("oasis register success, appCode={}, address={}", properties.getAppCode(), localAddress());
        } else {
            log.warn("oasis register failed, appCode={}, address={}", properties.getAppCode(), localAddress());
        }
        return ok;
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
}
