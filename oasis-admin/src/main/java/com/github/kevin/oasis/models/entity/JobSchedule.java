package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调度状态信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobSchedule {

    private Long id;

    private Long jobId;

    private Integer shardId;

    private Long nextTriggerTime;

    private String misfireStrategy;

    private Boolean triggerStatus;

    private Long version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
