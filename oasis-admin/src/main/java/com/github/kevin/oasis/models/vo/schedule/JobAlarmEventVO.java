package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警事件展示
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAlarmEventVO {

    private Long id;

    private Long jobId;

    private Long fireLogId;

    private String alarmType;

    private String alarmContent;

    private String notifyStatus;

    private Long triggerTime;

    /**
     * 逗号分隔的通知渠道列表
     */
    private String notifyChannels;
}
