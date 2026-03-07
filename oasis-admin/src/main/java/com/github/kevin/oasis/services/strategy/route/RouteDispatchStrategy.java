package com.github.kevin.oasis.services.strategy.route;

import com.github.kevin.oasis.services.DispatchResult;

public interface RouteDispatchStrategy {

    /**
     * 策略标识：ROUND / RANDOM / FAILOVER / BROADCAST
     */
    String route();

    DispatchResult dispatch(RouteDispatchContext context);
}
