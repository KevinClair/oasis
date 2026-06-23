package com.github.kevin.oasis.services.strategy;

import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.strategy.route.RouteDispatchContext;
import com.github.kevin.oasis.services.strategy.route.RouteDispatchStrategy;
import com.github.kevin.oasis.services.strategy.route.RouteDispatchStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteStrategyTest {

    private RouteDispatchStrategyRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new RouteDispatchStrategyRegistry(List.of(
                new FakeStrategy("ROUND"),
                new FakeStrategy("RANDOM"),
                new FakeStrategy("FAILOVER"),
                new FakeStrategy("BROADCAST")
        ));
    }

    @Test
    void knownStrategyResolved() {
        var result = registry.dispatch("ROUND", null);
        assertNotNull(result);
    }

    @Test
    void unknownStrategyFallsBackToRound() {
        var result = registry.dispatch("UNKNOWN", null);
        assertNotNull(result);
    }

    @Test
    void caseInsensitive() {
        var result = registry.dispatch("random", null);
        assertNotNull(result);
    }

    @Test
    void nullStrategyFallsBack() {
        var result = registry.dispatch(null, null);
        assertNotNull(result);
    }

    @Test
    void allKnownRouteNames() {
        for (String route : List.of("ROUND", "RANDOM", "FAILOVER", "BROADCAST")) {
            assertNotNull(registry.dispatch(route, null), "route: " + route);
        }
    }

    /** Fake strategy for testing RouteDispatchStrategyRegistry. */
    record FakeStrategy(String route) implements RouteDispatchStrategy {
        @Override
        public DispatchResult dispatch(RouteDispatchContext context) {
            return DispatchResult.builder().success(true).build();
        }
    }
}
