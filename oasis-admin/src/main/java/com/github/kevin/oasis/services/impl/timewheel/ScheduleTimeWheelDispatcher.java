package com.github.kevin.oasis.services.impl.timewheel;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.JobInfoDao;
import com.github.kevin.oasis.dao.JobScheduleDao;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.entity.JobSchedule;
import com.github.kevin.oasis.services.JobTriggerExecutorService;
import com.github.kevin.oasis.services.impl.ShardLeaseCoordinator;
import com.github.kevin.oasis.services.strategy.schedule.ScheduleTypeStrategyRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 独立时间轮调度组件：
 * 1) 预加载未来窗口任务到时间轮
 * 2) tick 线程仅消费当前槽到期任务
 * 3) 触发阶段仍用 CAS 抢占，保证集群幂等
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleTimeWheelDispatcher {

    private static final Long DISABLED_NEXT_TRIGGER_TIME = Long.MAX_VALUE;

    private final SchedulerRuntimeProperties runtimeProperties;
    private final JobScheduleDao jobScheduleDao;
    private final JobInfoDao jobInfoDao;
    private final JobTriggerExecutorService jobTriggerExecutorService;
    private final ShardLeaseCoordinator shardLeaseCoordinator;
    private final ScheduleTypeStrategyRegistry scheduleTypeStrategyRegistry;

    private ScheduleTimeWheel timeWheel;

    @PostConstruct
    public void init() {
        timeWheel = new ScheduleTimeWheel(
                runtimeProperties.getTimeWheelTickMs(),
                runtimeProperties.getTimeWheelSlotCount()
        );
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.time-wheel-preload-interval-ms:1000}")
    public void preloadWindowTasks() {
        if (!runtimeProperties.isEnabled() || !runtimeProperties.isTimeWheelEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        long end = now + runtimeProperties.getTimeWheelPreloadWindowMs();
        List<JobSchedule> schedules = selectWindowSchedules(now, end);
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        for (JobSchedule schedule : schedules) {
            timeWheel.addTask(ScheduleWheelTask.builder()
                    .jobId(schedule.getJobId())
                    .expectedVersion(schedule.getVersion())
                    .expectedNextTriggerTime(schedule.getNextTriggerTime())
                    .build(), now);
        }
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.time-wheel-tick-ms:1000}")
    public void consumeTick() {
        if (!runtimeProperties.isEnabled() || !runtimeProperties.isTimeWheelEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        List<ScheduleWheelTask> dueTasks = timeWheel.pollDueTasks(now);
        if (dueTasks.isEmpty()) {
            return;
        }

        for (ScheduleWheelTask task : dueTasks) {
            processDueTask(task, now);
        }
    }

    private List<JobSchedule> selectWindowSchedules(long now, long end) {
        int limit = runtimeProperties.getTimeWheelPreloadBatchSize();
        if (runtimeProperties.isShardLeaseEnabled()) {
            List<Integer> ownedShardIds = shardLeaseCoordinator.getOwnedShardIds();
            if (ownedShardIds == null || ownedShardIds.isEmpty()) {
                return Collections.emptyList();
            }
            return jobScheduleDao.selectSchedulesInWindowByShards(now, end, limit, ownedShardIds);
        }
        return jobScheduleDao.selectSchedulesInWindow(now, end, limit);
    }

    private void processDueTask(ScheduleWheelTask task, long now) {
        try {
            JobInfo jobInfo = jobInfoDao.selectById(task.getJobId());
            TriggerPlan plan = buildTriggerPlan(jobInfo, now);

            int claimed = jobScheduleDao.claimAndUpdateNextTrigger(
                    task.getJobId(),
                    task.getExpectedVersion(),
                    task.getExpectedNextTriggerTime(),
                    plan.nextTriggerTime(),
                    plan.triggerStatus()
            );

            if (claimed <= 0 || !plan.fireNow()) {
                return;
            }

            // CAS 成功后才真正触发，确保多节点场景下同一时刻仅一个节点发起执行。
            jobTriggerExecutorService.trigger(jobInfo, "SCHEDULE", null, 1);
        } catch (Exception e) {
            log.error("time wheel process task failed, jobId={}", task.getJobId(), e);
        }
    }

    private TriggerPlan buildTriggerPlan(JobInfo jobInfo, long now) {
        if (jobInfo == null || Boolean.FALSE.equals(jobInfo.getStatus())) {
            return new TriggerPlan(DISABLED_NEXT_TRIGGER_TIME, false, false);
        }

        if (scheduleTypeStrategyRegistry.isOneShot(jobInfo.getScheduleType())) {
            return new TriggerPlan(DISABLED_NEXT_TRIGGER_TIME, false, true);
        }

        Long nextTriggerTime = scheduleTypeStrategyRegistry.nextTriggerTime(
                jobInfo.getScheduleType(),
                jobInfo.getScheduleConf(),
                now
        );
        if (nextTriggerTime == null || nextTriggerTime <= now) {
            log.warn("invalid schedule conf in time wheel, disable job, jobId={}, type={}, conf={}",
                    jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf());
            return new TriggerPlan(DISABLED_NEXT_TRIGGER_TIME, false, false);
        }
        return new TriggerPlan(nextTriggerTime, true, true);
    }

    private record TriggerPlan(Long nextTriggerTime, Boolean triggerStatus, boolean fireNow) {
    }
}

