package com.github.kevin.oasis.services.strategy.schedule;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * CRON 调度策略。
 */
@Component
public class CronScheduleTypeStrategy implements ScheduleTypeStrategy {

    @Override
    public String type() {
        return "CRON";
    }

    @Override
    public Long nextTriggerTime(String scheduleConf, Long nowTime) {
        try {
            CronExpression expression = CronExpression.parse(scheduleConf.trim());
            LocalDateTime next = expression.next(LocalDateTime.ofInstant(Instant.ofEpochMilli(nowTime), ZoneId.systemDefault()));
            return next == null ? null : next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }
}
