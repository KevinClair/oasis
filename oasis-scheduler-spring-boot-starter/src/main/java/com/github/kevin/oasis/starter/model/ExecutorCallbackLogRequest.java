package com.github.kevin.oasis.starter.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutorCallbackLogRequest {

    private Long fireLogId;

    private Integer seqNo;

    private Long logTime;

    private String logContent;
}
