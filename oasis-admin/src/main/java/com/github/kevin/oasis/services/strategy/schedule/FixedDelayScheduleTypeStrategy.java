package com.github.kevin.oasis.services.strategy.schedule;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 固定延迟调度策略。
 */
@Component
public class FixedDelayScheduleTypeStrategy implements ScheduleTypeStrategy {

    @Override
    public String type() {
        return "FIXED_DELAY";
    }

    @Override
    public Long nextTriggerTime(String scheduleConf, Long nowTime) {
        Long delayMs = parseDurationToMillis(scheduleConf);
        if (delayMs == null || delayMs <= 0L) {
            return null;
        }
        return nowTime + delayMs;
    }

    private Long parseDurationToMillis(String conf) {
        if (conf == null || conf.isBlank()) {
            return null;
        }
        String value = conf.trim().toLowerCase(Locale.ROOT);
        try {
            if (value.endsWith("ms")) {
                return Long.parseLong(value.substring(0, value.length() - 2));
            }
            if (value.endsWith("s")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1000L;
            }
            if (value.endsWith("m")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 60_000L;
            }
            if (value.endsWith("h")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 3_600_000L;
            }
            if (value.matches("^\\d+$")) {
                return Long.parseLong(value) * 1000L;
            }
        } catch (NumberFormatException ignore) {
            return null;
        }
        return null;
    }
}
