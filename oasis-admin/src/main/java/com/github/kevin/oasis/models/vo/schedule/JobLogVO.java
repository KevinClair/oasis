package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调度日志展示对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobLogVO {

    private Long id;

    private Long jobId;

    private String appCode;

    private String jobName;

    private Long triggerTime;

    private Long finishTime;

    private String triggerType;

    private String executorAddress;

    private String status;

    private Integer attemptNo;

    private String errorMessage;

    private String traceId;
}
