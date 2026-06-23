package com.github.kevin.oasis.services.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 错失触发策略逻辑测试。
 */
class MisfireStrategyTest {

    private static final long THRESHOLD_MS = 60_000L;

    @Test
    void fireOnceWhenWithinThreshold() {
        long now = 200000L;
        long triggerTime = now - 30_000L; // 30s ago, within threshold
        assertFalse(isMisfire("FIRE_ONCE", triggerTime, now));
        assertFalse(isMisfire("IGNORE", triggerTime, now));
    }

    @Test
    void ignoreWhenBeyondThreshold() {
        long now = 200000L;
        long triggerTime = now - 90_000L; // 90s ago, beyond threshold
        assertFalse(isMisfire("FIRE_ONCE", triggerTime, now));
        assertTrue(isMisfire("IGNORE", triggerTime, now));
    }

    @Test
    void nullMisfireStrategyDefaultsToFireOnce() {
        long now = 200000L;
        long triggerTime = now - 90_000L;
        assertFalse(isMisfire(null, triggerTime, now));
        assertFalse(isMisfire("", triggerTime, now));
        assertFalse(isMisfire("  ", triggerTime, now));
    }

    @Test
    void nullTriggerTimeNotMisfire() {
        assertFalse(isMisfire("IGNORE", null, 100000L));
    }

    @Test
    void futureTriggerTimeNotMisfire() {
        long now = 100000L;
        long triggerTime = now + 5000L;
        assertFalse(isMisfire("IGNORE", triggerTime, now));
    }

    @Test
    void exactBoundaryNotMisfire() {
        long now = 200000L;
        long triggerTime = now - THRESHOLD_MS + 1; // 1ms under threshold
        assertFalse(isMisfire("IGNORE", triggerTime, now));
    }

    @Test
    void caseInsensitiveStrategyName() {
        long now = 200000L;
        long triggerTime = now - 90_000L;
        assertTrue(isMisfire("ignore", triggerTime, now));
        assertTrue(isMisfire(" Ignore ", triggerTime, now));
    }

    private static boolean isMisfire(String misfireStrategy, Long expectedTriggerTime, long now) {
        if (expectedTriggerTime == null) {
            return false;
        }
        long lag = now - expectedTriggerTime;
        if (lag < THRESHOLD_MS) {
            return false;
        }
        return "IGNORE".equalsIgnoreCase(misfireStrategy != null ? misfireStrategy.trim() : "");
    }
}
