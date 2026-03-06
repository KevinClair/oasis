package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警通知发送记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAlarmNotifyRecord {

    private Long id;

    private Long alarmEventId;

    private String channel;

    private String receiver;

    private String sendStatus;

    private String responseMessage;

    private Long sendTime;

    private LocalDateTime createTime;
}
