package com.github.kevin.oasis.services.strategy.schedule;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 调度类型策略注册中心。
 */
@Component
public class ScheduleTypeStrategyRegistry {

    private final Map<String, ScheduleTypeStrategy> strategyMap;

    public ScheduleTypeStrategyRegistry(List<ScheduleTypeStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(s -> normalize(s.type()), Function.identity()));
    }

    public Long nextTriggerTime(String scheduleType, String scheduleConf, Long nowTime) {
        ScheduleTypeStrategy strategy = strategyMap.get(normalize(scheduleType));
        if (strategy == null) {
            return null;
        }
        return strategy.nextTriggerTime(scheduleConf, nowTime);
    }

    public boolean isOneShot(String scheduleType) {
        ScheduleTypeStrategy strategy = strategyMap.get(normalize(scheduleType));
        return strategy != null && strategy.oneShot();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
