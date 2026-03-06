package com.github.kevin.oasis.starter.service;

import com.github.kevin.oasis.starter.handler.OasisJobHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for all Oasis job handlers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobHandlerRegistry {

    private final ApplicationContext applicationContext;

    private final Map<String, OasisJobHandler> handlers = new ConcurrentHashMap<>();

    public void init() {
        Map<String, OasisJobHandler> beanMap = applicationContext.getBeansOfType(OasisJobHandler.class);
        handlers.clear();
        beanMap.values().forEach(handler -> {
            String name = handler.handlerName();
            if (handlers.containsKey(name)) {
                log.warn("duplicate handlerName detected: {}", name);
            }
            handlers.put(name, handler);
        });
        log.info("oasis handler registry initialized, count={}", handlers.size());
    }

    public Optional<OasisJobHandler> get(String handlerName) {
        return Optional.ofNullable(handlers.get(handlerName));
    }
}
