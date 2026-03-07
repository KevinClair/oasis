package com.github.kevin.oasis.services.impl.timewheel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时间轮内的调度任务快照。
 * 使用 expectedVersion + expectedNextTriggerTime 做 CAS 抢占，防止重复触发。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleWheelTask {

    private Long jobId;

    private Long expectedVersion;

    private Long expectedNextTriggerTime;

    /**
     * 剩余圈数，>0 表示还要再转若干圈才能到期。
     */
    private int rounds;
}

