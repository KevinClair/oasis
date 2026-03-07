package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.ExecutorNodeDao;
import com.github.kevin.oasis.dao.JobInfoDao;
import com.github.kevin.oasis.dao.JobScheduleDao;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.entity.JobSchedule;
import com.github.kevin.oasis.services.JobTriggerExecutorService;
import com.github.kevin.oasis.services.strategy.schedule.ScheduleTypeStrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 调度扫描器：扫描到期任务并触发执行
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleScannerService {

    private static final Long DISABLED_NEXT_TRIGGER_TIME = Long.MAX_VALUE;

    private final SchedulerRuntimeProperties runtimeProperties;
    private final JobScheduleDao jobScheduleDao;
    private final JobInfoDao jobInfoDao;
    private final JobTriggerExecutorService jobTriggerExecutorService;
    private final ExecutorNodeDao executorNodeDao;
    private final ShardLeaseCoordinator shardLeaseCoordinator;
    private final ScheduleTypeStrategyRegistry scheduleTypeStrategyRegistry;

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.scan-interval-ms:1000}")
    public void scanAndTrigger() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }
        if (runtimeProperties.isTimeWheelEnabled()) {
            // 时间轮开启时，本扫描器仅作为兜底保留，不再执行全量扫描触发。
            return;
        }

        long now = System.currentTimeMillis();
        List<JobSchedule> dueSchedules;
        if (runtimeProperties.isShardLeaseEnabled()) {
            // 只扫描本节点持有租约的分片，避免所有节点全量扫描同一批任务。
            List<Integer> ownedShardIds = shardLeaseCoordinator.getOwnedShardIds();
            if (ownedShardIds == null || ownedShardIds.isEmpty()) {
                return;
            }
            dueSchedules = jobScheduleDao.selectDueSchedulesByShards(now, runtimeProperties.getScanLimit(), ownedShardIds);
        } else {
            dueSchedules = jobScheduleDao.selectDueSchedules(now, runtimeProperties.getScanLimit());
        }

        if (dueSchedules == null || dueSchedules.isEmpty()) {
            return;
        }

        for (JobSchedule schedule : dueSchedules) {
            processSingleSchedule(schedule, now);
        }
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.executor-offline-check-interval-ms:5000}")
    public void refreshExecutorStatus() {
        long expireBefore = System.currentTimeMillis() - runtimeProperties.getExecutorHeartbeatTimeoutMs();
        int updated = executorNodeDao.markOfflineByHeartbeatBefore(expireBefore);
        if (updated > 0) {
            log.info("mark offline executors done, count={}", updated);
        }
    }

    private void processSingleSchedule(JobSchedule schedule, long now) {
        try {
            JobInfo jobInfo = jobInfoDao.selectById(schedule.getJobId());
            TriggerPlan plan = buildTriggerPlan(jobInfo, now);

            // CAS 抢占：只有版本号和原触发时间都匹配的节点才能推进 next_trigger_time。
            int claimed = jobScheduleDao.claimAndUpdateNextTrigger(
                    schedule.getJobId(),
                    schedule.getVersion(),
                    schedule.getNextTriggerTime(),
                    plan.nextTriggerTime(),
                    plan.triggerStatus()
            );

            if (claimed <= 0 || !plan.fireNow()) {
                return;
            }

            // 抢占成功后再触发，避免多节点重复执行。
            jobTriggerExecutorService.trigger(jobInfo, "SCHEDULE", null, 1);
        } catch (Exception e) {
            log.error("process schedule failed, jobId={}", schedule.getJobId(), e);
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
            log.warn("invalid schedule conf, disable job, jobId={}, type={}, conf={}",
                    jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf());
            return new TriggerPlan(DISABLED_NEXT_TRIGGER_TIME, false, false);
        }

        return new TriggerPlan(nextTriggerTime, true, true);
    }

    private record TriggerPlan(Long nextTriggerTime, Boolean triggerStatus, boolean fireNow) {
    }
}
