package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.services.strategy.schedule.ScheduleTypeStrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 触发计划计算器：从 ScheduleTimeWheelDispatcher 和 ScheduleScannerService
 * 中提取的公共逻辑，统一计算下一次触发计划。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TriggerPlanComputer {

    static final Long DISABLED_NEXT_TRIGGER_TIME = Long.MAX_VALUE;
    static final long MISFIRE_THRESHOLD_MS = 60_000L;

    private final ScheduleTypeStrategyRegistry scheduleTypeStrategyRegistry;

    /**
     * @param jobInfo 任务信息
     * @param now     当前时间戳
     * @param refTime 计算下一次触发时间的参考时间：
     *                misfire 时使用预期触发时间逐次追赶；
     *                正常时使用 now 跳过微小延迟。
     */
    public TriggerPlan compute(JobInfo jobInfo, long now, long refTime) {
        if (jobInfo == null || Boolean.FALSE.equals(jobInfo.getStatus())) {
            return new TriggerPlan(DISABLED_NEXT_TRIGGER_TIME, false, false);
        }

        if (scheduleTypeStrategyRegistry.isOneShot(jobInfo.getScheduleType())) {
            return new TriggerPlan(DISABLED_NEXT_TRIGGER_TIME, false, true);
        }

        long overdueMs = now - refTime;
        boolean isMisfire = overdueMs > MISFIRE_THRESHOLD_MS;
        long baseTime = isMisfire ? refTime : now;

        Long nextTriggerTime = scheduleTypeStrategyRegistry.nextTriggerTime(
                jobInfo.getScheduleType(),
                jobInfo.getScheduleConf(),
                baseTime
        );
        if (nextTriggerTime == null || nextTriggerTime <= now) {
            log.warn("invalid schedule conf, disable job, jobId={}, type={}, conf={}",
                    jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf());
            return new TriggerPlan(DISABLED_NEXT_TRIGGER_TIME, false, false);
        }
        return new TriggerPlan(nextTriggerTime, true, true);
    }

    public record TriggerPlan(Long nextTriggerTime, Boolean triggerStatus, boolean fireNow) {
    }
}
