package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.ExecutorNodeDao;
import com.github.kevin.oasis.dao.JobFireLogDao;
import com.github.kevin.oasis.dao.JobInfoDao;
import com.github.kevin.oasis.dao.JobScheduleDao;
import com.github.kevin.oasis.models.entity.JobFireLog;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.entity.JobSchedule;
import com.github.kevin.oasis.services.JobTriggerExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 调度扫描器：扫描到期任务并触发执行
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleScannerService {

    private final SchedulerRuntimeProperties runtimeProperties;
    private final JobScheduleDao jobScheduleDao;
    private final JobInfoDao jobInfoDao;
    private final JobFireLogDao jobFireLogDao;
    private final JobTriggerExecutorService jobTriggerExecutorService;
    private final ExecutorNodeDao executorNodeDao;
    private final ShardLeaseCoordinator shardLeaseCoordinator;
    private final TriggerPlanComputer triggerPlanComputer;

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

        // 批量加载 JobInfo，避免 N+1 查询
        Map<Long, JobInfo> jobInfoMap = batchLoadJobInfoFromSchedules(dueSchedules);
        for (JobSchedule schedule : dueSchedules) {
            JobInfo jobInfo = jobInfoMap.get(schedule.getJobId());
            processSingleSchedule(schedule, now, jobInfo);
        }
    }

    private Map<Long, JobInfo> batchLoadJobInfoFromSchedules(List<JobSchedule> schedules) {
        List<Long> jobIds = schedules.stream()
                .map(JobSchedule::getJobId)
                .distinct()
                .collect(Collectors.toList());
        List<JobInfo> jobInfos = jobInfoDao.selectByIds(jobIds);
        return jobInfos.stream()
                .collect(Collectors.toMap(JobInfo::getId, Function.identity()));
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.executor-offline-check-interval-ms:5000}")
    public void refreshExecutorStatus() {
        long expireBefore = System.currentTimeMillis() - runtimeProperties.getExecutorHeartbeatTimeoutMs();
        int updated = executorNodeDao.markOfflineByHeartbeatBefore(expireBefore);
        if (updated > 0) {
            log.info("mark offline executors done, count={}", updated);
        }
    }

    /**
     * 执行超时看门狗：将 trigger_time + timeout_seconds 已过期的 RUNNING 记录标记为 TIMEOUT。
     * executor 崩溃或网络断开导致 callback 丢失时，由此兜底恢复。
     */
    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.timeout-check-interval-ms:10000}")
    public void checkExecutionTimeout() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        List<JobFireLog> timeoutLogs = jobFireLogDao.selectTimeoutRunning(now);
        if (timeoutLogs == null || timeoutLogs.isEmpty()) {
            return;
        }

        for (JobFireLog timeoutLog : timeoutLogs) {
            try {
                int updated = jobFireLogDao.updateToTimeout(timeoutLog.getId(), now);
                if (updated > 0) {
                    log.warn("execution timeout, fireLogId={}, jobId={}, triggerTime={}",
                            timeoutLog.getId(), timeoutLog.getJobId(), timeoutLog.getTriggerTime());
                }
            } catch (Exception e) {
                log.error("mark timeout failed, fireLogId={}", timeoutLog.getId(), e);
            }
        }
    }

    private void processSingleSchedule(JobSchedule schedule, long now, JobInfo jobInfo) {
        try {
            TriggerPlanComputer.TriggerPlan plan = triggerPlanComputer.compute(jobInfo, now, schedule.getNextTriggerTime());

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
}
