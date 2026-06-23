package com.github.kevin.oasis.services.strategy;

import com.github.kevin.oasis.services.strategy.schedule.CronScheduleTypeStrategy;
import com.github.kevin.oasis.services.strategy.schedule.FixedDelayScheduleTypeStrategy;
import com.github.kevin.oasis.services.strategy.schedule.OnceScheduleTypeStrategy;
import com.github.kevin.oasis.services.strategy.schedule.ScheduleTypeStrategy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleStrategyTest {

    private final ScheduleTypeStrategy cron = new CronScheduleTypeStrategy();
    private final ScheduleTypeStrategy fixedDelay = new FixedDelayScheduleTypeStrategy();
    private final ScheduleTypeStrategy once = new OnceScheduleTypeStrategy();

    @Test
    void cronNextTriggerIsInFuture() {
        long now = System.currentTimeMillis();
        Long next = cron.nextTriggerTime("* * * * * *", now);
        assertNotNull(next);
        assertTrue(next > now);
    }

    @Test
    void cronInvalidExpressionReturnsNull() {
        Long next = cron.nextTriggerTime("invalid", System.currentTimeMillis());
        assertNull(next);
    }

    @Test
    void fixedDelaySeconds() {
        long now = 1000000L;
        Long next = fixedDelay.nextTriggerTime("30s", now);
        assertEquals(now + 30_000L, next);
    }

    @Test
    void fixedDelayMinutes() {
        long now = 1000000L;
        Long next = fixedDelay.nextTriggerTime("5m", now);
        assertEquals(now + 300_000L, next);
    }

    @Test
    void fixedDelayMillis() {
        long now = 1000000L;
        Long next = fixedDelay.nextTriggerTime("100ms", now);
        assertEquals(now + 100L, next);
    }

    @Test
    void fixedDelayDefaultUnit() {
        long now = 1000000L;
        Long next = fixedDelay.nextTriggerTime("60", now);
        assertEquals(now + 60_000L, next);
    }

    @Test
    void fixedDelayInvalidReturnsNull() {
        assertNull(fixedDelay.nextTriggerTime("", 0L));
        assertNull(fixedDelay.nextTriggerTime(null, 0L));
        assertNull(fixedDelay.nextTriggerTime("abc", 0L));
    }

    @Test
    void onceWithTimestamp() {
        long future = System.currentTimeMillis() + 60_000L;
        Long next = once.nextTriggerTime(String.valueOf(future), System.currentTimeMillis());
        assertEquals(future, next);
    }

    @Test
    void oncePastTimestampReturnsNull() {
        long past = System.currentTimeMillis() - 60_000L;
        Long next = once.nextTriggerTime(String.valueOf(past), System.currentTimeMillis());
        assertNull(next);
    }

    @Test
    void onceWithDateTime() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        String conf = future.toString();
        Long next = once.nextTriggerTime(conf, System.currentTimeMillis());
        assertNotNull(next);
        assertTrue(next > System.currentTimeMillis());
    }

    @Test
    void onceInvalidReturnsNull() {
        assertNull(once.nextTriggerTime("", 0L));
        assertNull(once.nextTriggerTime(null, 0L));
    }

    @Test
    void oneShotFlag() {
        assertTrue(once.oneShot());
        assertFalse(cron.oneShot());
    }
}
