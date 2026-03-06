package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警通知记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmNotifyRecordVO {

    private Long id;

    private Long alarmEventId;

    private String channel;

    private String receiver;

    private String sendStatus;

    private String responseMessage;

    private Long sendTime;
}
