package com.github.kevin.oasis.services.impl.timewheel;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.JobInfoDao;
import com.github.kevin.oasis.dao.JobScheduleDao;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.entity.JobSchedule;
import com.github.kevin.oasis.services.JobTriggerExecutorService;
import com.github.kevin.oasis.services.impl.ShardLeaseCoordinator;
import com.github.kevin.oasis.services.impl.TriggerPlanComputer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final SchedulerRuntimeProperties runtimeProperties;
    private final JobScheduleDao jobScheduleDao;
    private final JobInfoDao jobInfoDao;
    private final JobTriggerExecutorService jobTriggerExecutorService;
    private final ShardLeaseCoordinator shardLeaseCoordinator;
    private final TriggerPlanComputer triggerPlanComputer;

    private ScheduleTimeWheel timeWheel;

    @PostConstruct
    public void init() {
        timeWheel = new ScheduleTimeWheel(
                runtimeProperties.getTimeWheelTickMs(),
                runtimeProperties.getTimeWheelSlotCount()
        );
    }

    /**
     * 当 job 被 disable/delete/调度变更时，清理时间轮中该 job 的 dedup 条目，
     * 防止 key 永久残留导致内存泄漏。
     */
    public void removeJobFromWheel(Long jobId) {
        if (timeWheel != null) {
            timeWheel.removeDedupByJobId(jobId);
        }
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

    /**
     * 兜底清理：每 5 分钟扫描 dedup map，移除 expectedNextTriggerTime 已过期超过 5 分钟的条目。
     * 主路径依靠 removeJobFromWheel，此方法作为兜底防止异常情况下的泄漏。
     */
    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.time-wheel-dedup-cleanup-interval-ms:300000}")
    public void cleanupExpiredDedup() {
        if (timeWheel == null) {
            return;
        }
        long expireBeforeMs = System.currentTimeMillis() - 300_000L;
        timeWheel.cleanExpiredDedup(expireBeforeMs);
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

        // 批量加载 JobInfo，避免 N+1 查询
        Map<Long, JobInfo> jobInfoMap = batchLoadJobInfo(dueTasks);
        for (ScheduleWheelTask task : dueTasks) {
            JobInfo jobInfo = jobInfoMap.get(task.getJobId());
            processDueTask(task, now, jobInfo);
        }
    }

    private Map<Long, JobInfo> batchLoadJobInfo(List<ScheduleWheelTask> tasks) {
        List<Long> jobIds = tasks.stream()
                .map(ScheduleWheelTask::getJobId)
                .distinct()
                .collect(Collectors.toList());
        List<JobInfo> jobInfos = jobInfoDao.selectByIds(jobIds);
        return jobInfos.stream()
                .collect(Collectors.toMap(JobInfo::getId, Function.identity()));
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

    private void processDueTask(ScheduleWheelTask task, long now, JobInfo jobInfo) {
        try {
            TriggerPlanComputer.TriggerPlan plan = triggerPlanComputer.compute(jobInfo, now, task.getExpectedNextTriggerTime());

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
}

