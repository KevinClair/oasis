package com.github.kevin.oasis.utils;

import org.springframework.scheduling.support.CronExpression;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * 任务下次触发时间计算工具
 */
public final class ScheduleNextTimeCalculator {

    private ScheduleNextTimeCalculator() {
    }

    public static Long computeNextTriggerTime(String scheduleType, String scheduleConf, Long nowTime) {
        if (scheduleType == null || scheduleConf == null || scheduleConf.isBlank()) {
            return null;
        }

        String type = scheduleType.trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "CRON" -> computeCronNext(scheduleConf, nowTime);
            case "FIXED_DELAY" -> computeFixedDelayNext(scheduleConf, nowTime);
            case "ONCE" -> computeOnceNext(scheduleConf, nowTime);
            default -> null;
        };
    }

    private static Long computeCronNext(String cron, Long nowTime) {
        try {
            CronExpression expression = CronExpression.parse(cron.trim());
            LocalDateTime next = expression.next(LocalDateTime.ofInstant(Instant.ofEpochMilli(nowTime), ZoneId.systemDefault()));
            return next == null ? null : next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Long computeFixedDelayNext(String conf, Long nowTime) {
        Long delayMs = parseDurationToMillis(conf);
        if (delayMs == null || delayMs <= 0L) {
            return null;
        }
        return nowTime + delayMs;
    }

    private static Long computeOnceNext(String conf, Long nowTime) {
        String value = conf.trim();
        if (value.matches("^\\d{10,13}$")) {
            long raw = Long.parseLong(value);
            if (value.length() == 10) {
                raw = raw * 1000L;
            }
            return raw >= nowTime ? raw : null;
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(value);
            long millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return millis >= nowTime ? millis : null;
        } catch (DateTimeParseException ignore) {
            return null;
        }
    }

    private static Long parseDurationToMillis(String conf) {
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
