package com.github.kevin.oasis.models.vo.schedule;

import lombok.Data;

/**
 * 任务列表查询
 */
@Data
public class JobListRequest {

    private Integer current = 1;

    private Integer size = 10;

    private String appCode;

    private String jobName;

    private String handlerName;

    private String scheduleType;

    private Boolean status;
}
