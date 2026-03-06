package com.github.kevin.oasis.starter.context;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runtime context passed to handlers.
 */
@Getter
public class OasisJobContext {

    private final Long fireLogId;

    private final Integer attemptNo;

    private final String triggerParam;

    private final List<LogLine> logs = Collections.synchronizedList(new ArrayList<>());

    public OasisJobContext(Long fireLogId, Integer attemptNo, String triggerParam) {
        this.fireLogId = fireLogId;
        this.attemptNo = attemptNo;
        this.triggerParam = triggerParam;
    }

    public void log(String message) {
        logs.add(new LogLine(System.currentTimeMillis(), message));
    }

    public record LogLine(Long time, String content) {}
}
