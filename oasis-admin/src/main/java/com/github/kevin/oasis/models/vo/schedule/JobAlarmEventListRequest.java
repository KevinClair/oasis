package com.github.kevin.oasis.models.vo.schedule;

import lombok.Data;

/**
 * 告警事件列表查询请求
 */
@Data
public class JobAlarmEventListRequest {

    private Integer current = 1;

    private Integer size = 10;

    /**
     * 可选任务ID。
     * 当为空时返回全局告警事件列表；有值时仅返回对应任务告警事件。
     */
    private Long jobId;

    private String alarmType;

    private String notifyStatus;
}
