package com.github.kevin.oasis.services.alarm;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 告警通知通道注册中心。
 * 自动发现所有 AlarmNotifier Bean 并按 channel name 索引。
 */
@Component
public class AlarmNotifierRegistry {

    private final Map<String, AlarmNotifier> notifierMap;

    public AlarmNotifierRegistry(List<AlarmNotifier> notifiers) {
        this.notifierMap = notifiers.stream()
                .collect(Collectors.toMap(n -> normalize(n.channel()), Function.identity()));
    }

    /**
     * 获取指定通道的通知器，找不到返回 null。
     */
    public AlarmNotifier get(String channel) {
        return notifierMap.get(normalize(channel));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
