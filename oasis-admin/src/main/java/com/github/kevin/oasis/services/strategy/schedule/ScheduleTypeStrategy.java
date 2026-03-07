package com.github.kevin.oasis.services.strategy.schedule;

/**
 * 调度类型策略接口。
 */
public interface ScheduleTypeStrategy {

    /**
     * 策略标识，例如 CRON / FIXED_DELAY / ONCE。
     */
    String type();

    /**
     * 是否一次性触发策略（触发后应关闭调度）。
     */
    default boolean oneShot() {
        return false;
    }

    /**
     * 计算下一次触发时间（毫秒时间戳）。
     * 返回 null 表示配置非法或不再触发。
     */
    Long nextTriggerTime(String scheduleConf, Long nowTime);
}
