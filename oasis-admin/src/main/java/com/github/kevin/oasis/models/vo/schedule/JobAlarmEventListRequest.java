package com.github.kevin.oasis.models.vo.schedule;

import lombok.Data;

/**
 * 告警事件列表查询请求
 */
@Data
public class JobAlarmEventListRequest {

    private Integer current = 1;

    private Integer size = 10;

    private String alarmType;

    private String notifyStatus;
}
