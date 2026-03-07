package com.github.kevin.oasis.services.strategy.schedule;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * 单次调度策略。
 */
@Component
public class OnceScheduleTypeStrategy implements ScheduleTypeStrategy {

    @Override
    public String type() {
        return "ONCE";
    }

    @Override
    public boolean oneShot() {
        return true;
    }

    @Override
    public Long nextTriggerTime(String scheduleConf, Long nowTime) {
        if (scheduleConf == null || scheduleConf.isBlank()) {
            return null;
        }
        String value = scheduleConf.trim();
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
}
