package com.github.kevin.oasis.global.oauth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    @Test
    void allowsRequestsWithinLimit() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire("192.168.1.1"), "attempt " + (i + 1));
        }
    }

    @Test
    void blocksAfterExceedingLimit() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        String ip = "10.0.0.1";
        // First 10 should succeed
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire(ip), "attempt " + (i + 1));
        }
        // 11th should be blocked
        assertFalse(limiter.tryAcquire(ip));
    }

    @Test
    void differentIpsAreIndependent() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire("192.168.1.1"));
        }
        // Same IP blocked
        assertFalse(limiter.tryAcquire("192.168.1.1"));
        // Different IP still allowed
        assertTrue(limiter.tryAcquire("192.168.1.2"));
    }
}
