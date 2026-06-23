package com.github.kevin.oasis.global.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的内存登录频率限制器。
 * 基于 IP 地址限制，防止暴力破解。
 */
@Component
@Slf4j
public class LoginRateLimiter {

    /** 每个 IP 的最大尝试次数 */
    private static final int MAX_ATTEMPTS = 10;

    /** 计数窗口（毫秒） */
    private static final long WINDOW_MS = 60_000L;

    /** 超过阈值后的封锁时间（毫秒） */
    private static final long BLOCK_MS = 300_000L;

    private static final int MAX_CACHE_SIZE = 10_000;

    /** key: clientIp, value: AttemptWindow */
    private final ConcurrentHashMap<String, AttemptWindow> cache = new ConcurrentHashMap<>();

    public boolean tryAcquire(String clientIp) {
        long now = System.currentTimeMillis();

        AttemptWindow window = cache.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new AttemptWindow(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (window.blockUntil > now) {
            log.warn("login blocked by rate limiter, ip={}, blockUntil={}", clientIp, window.blockUntil);
            return false;
        }

        if (window.count > MAX_ATTEMPTS) {
            window.blockUntil = now + BLOCK_MS;
            log.warn("login rate limit exceeded, ip={}, attempts={}, blocked for {}ms", clientIp, window.count, BLOCK_MS);
            return false;
        }

        cleanupIfNeeded();
        return true;
    }

    private void cleanupIfNeeded() {
        if (cache.size() > MAX_CACHE_SIZE) {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<String, AttemptWindow>> it = cache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, AttemptWindow> entry = it.next();
                AttemptWindow w = entry.getValue();
                if (w == null || (w.blockUntil < now && (now - w.windowStart) > WINDOW_MS * 2)) {
                    it.remove();
                }
            }
        }
    }

    private static class AttemptWindow {
        final long windowStart;
        int count;
        long blockUntil = 0;

        AttemptWindow(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
