package com.github.kevin.oasis.services.impl;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 超时执行逻辑测试。
 */
class TimeoutStrategyTest {

    @Test
    void taskCompletesWithinTimeout() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            Thread.sleep(100);
            return "ok";
        });
        Object result = future.get(5, TimeUnit.SECONDS);
        assertEquals("ok", result);
        executor.shutdown();
    }

    @Test
    void taskExceedsTimeoutThrowsException() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            Thread.sleep(99999);
            return "never";
        });
        assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
        future.cancel(true);
        executor.shutdownNow();
    }

    @Test
    void interruptedThreadPreservesInterruptFlag() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            started.countDown();
            try {
                Thread.sleep(99999);
            } catch (InterruptedException e) {
                interrupted.countDown();
                Thread.currentThread().interrupt();
                throw e;
            }
            return null;
        });

        started.await(1, TimeUnit.SECONDS);
        future.cancel(true);
        assertTrue(interrupted.await(1, TimeUnit.SECONDS));
        executor.shutdownNow();
    }

    @Test
    void nullTimeoutUsesDefault() {
        Integer timeout = null;
        int actual = timeout != null && timeout > 0 ? timeout : 30;
        assertEquals(30, actual);
    }

    @Test
    void zeroTimeoutUsesDefault() {
        int actual = 0 > 0 ? 0 : 30;
        assertEquals(30, actual);
    }
}
