package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.services.DispatchResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 路由策略注册中心。
 */
@Component
public class RouteDispatchStrategyRegistry {

    private static final String DEFAULT_ROUTE = "ROUND";

    private final Map<String, RouteDispatchStrategy> strategyMap;

    public RouteDispatchStrategyRegistry(List<RouteDispatchStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(s -> normalize(s.route()), Function.identity()));
    }

    public DispatchResult dispatch(String route, RouteDispatchContext context) {
        RouteDispatchStrategy strategy = strategyMap.getOrDefault(normalize(route), strategyMap.get(DEFAULT_ROUTE));
        if (strategy == null) {
            return DispatchResult.builder().success(false).errorMessage("未找到可用路由策略").build();
        }
        return strategy.dispatch(context);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
