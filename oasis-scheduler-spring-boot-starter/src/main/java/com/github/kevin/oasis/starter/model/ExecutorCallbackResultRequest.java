package com.github.kevin.oasis.starter.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutorCallbackResultRequest {

    private Long fireLogId;

    private Integer attemptNo;

    private String status;

    private String errorMessage;

    private String executorAddress;

    private Long finishTime;
}
