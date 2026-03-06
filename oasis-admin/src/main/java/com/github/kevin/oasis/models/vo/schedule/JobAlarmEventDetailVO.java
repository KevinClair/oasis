package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 告警事件详情
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAlarmEventDetailVO {

    private Long id;

    private Long jobId;

    private Long fireLogId;

    private String alarmType;

    private String alarmContent;

    private String notifyStatus;

    private Long triggerTime;

    private List<AlarmNotifyRecordVO> notifyRecords;
}
