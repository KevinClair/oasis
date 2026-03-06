package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警事件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAlarmEvent {

    private Long id;

    private Long jobId;

    private Long fireLogId;

    private String alarmType;

    private String alarmContent;

    private String notifyStatus;

    private Long triggerTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
