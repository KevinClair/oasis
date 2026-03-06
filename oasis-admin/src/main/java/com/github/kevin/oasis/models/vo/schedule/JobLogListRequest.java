package com.github.kevin.oasis.models.vo.schedule;

import lombok.Data;

/**
 * 调度日志查询
 */
@Data
public class JobLogListRequest {

    private Integer current = 1;

    private Integer size = 10;

    private Long logId;

    private Long jobId;

    private String appCode;

    private String status;

    private Long startTriggerTime;

    private Long endTriggerTime;
}
