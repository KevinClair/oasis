package com.github.kevin.oasis.services.alarm;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.*;
import com.github.kevin.oasis.models.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 告警扫描器：扫描失败/超时的执行日志，按告警策略生成告警事件。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmScannerService {

    private final SchedulerRuntimeProperties runtimeProperties;
    private final JobFireLogDao jobFireLogDao;
    private final JobInfoDao jobInfoDao;
    private final JobAlarmPolicyDao jobAlarmPolicyDao;
    private final AppAlarmTemplateDao appAlarmTemplateDao;
    private final JobAlarmEventDao jobAlarmEventDao;

    private volatile long lastScanTime = System.currentTimeMillis();

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.alarm-scan-interval-ms:30000}")
    public void scanFailedJobs() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        long since = lastScanTime;
        lastScanTime = now;

        // 查询最近时间窗口内的失败执行日志
        List<JobFireLog> failedLogs = jobFireLogDao.selectFailedSince(since);
        if (failedLogs == null || failedLogs.isEmpty()) {
            return;
        }

        for (JobFireLog fireLog : failedLogs) {
            try {
                processFailedLog(fireLog, now);
            } catch (Exception e) {
                log.error("process alarm for fireLog failed, fireLogId={}", fireLog.getId(), e);
            }
        }
    }

    private void processFailedLog(JobFireLog fireLog, long now) {
        Long jobId = fireLog.getJobId();
        JobInfo jobInfo = jobInfoDao.selectById(jobId);
        if (jobInfo == null) {
            return;
        }

        // 确定告警策略：优先使用 job 级策略，否则继承 app 级模板
        JobAlarmPolicy policy = jobAlarmPolicyDao.selectByJobId(jobId);
        AppAlarmTemplate template = null;

        if (policy != null && Boolean.TRUE.equals(policy.getEnabled())) {
            if (Boolean.TRUE.equals(policy.getInheritAppTemplate())) {
                template = appAlarmTemplateDao.selectByAppCode(jobInfo.getAppCode());
            }
        } else {
            template = appAlarmTemplateDao.selectByAppCode(jobInfo.getAppCode());
        }

        // 检查告警是否启用
        boolean enabled = policy != null && Boolean.TRUE.equals(policy.getEnabled())
                || (template != null && Boolean.TRUE.equals(template.getEnabled()));
        if (!enabled) {
            return;
        }

        // 静默期检查：避免频繁重复告警
        if (isInQuietPeriod(jobId, now, policy, template)) {
            return;
        }

        // 构建告警内容
        String title = "任务执行失败 - " + jobInfo.getJobName();
        String content = String.format(
                "应用: %s\n任务: %s\nHandler: %s\n执行日志ID: %d\n失败原因: %s",
                jobInfo.getAppCode(),
                jobInfo.getJobName(),
                jobInfo.getHandlerName(),
                fireLog.getId(),
                fireLog.getErrorMessage() != null ? fireLog.getErrorMessage() : "未知"
        );

        // 创建告警事件
        JobAlarmEvent event = JobAlarmEvent.builder()
                .jobId(jobId)
                .fireLogId(fireLog.getId())
                .alarmType("FAILED")
                .alarmContent(content)
                .notifyStatus("PENDING")
                .triggerTime(now)
                .build();
        jobAlarmEventDao.insert(event);

        log.info("alarm event created, jobId={}, fireLogId={}, title={}", jobId, fireLog.getId(), title);
    }

    private boolean isInQuietPeriod(Long jobId, long now, JobAlarmPolicy policy, AppAlarmTemplate template) {
        // 获取静默期（分钟）
        int quietMinutes = 10;
        if (policy != null && policy.getQuietPeriodMinutes() != null) {
            quietMinutes = policy.getQuietPeriodMinutes();
        } else if (template != null) {
            quietMinutes = template.getQuietPeriodMinutes();
        }

        long quietMs = quietMinutes * 60_000L;
        JobAlarmEvent latestEvent = jobAlarmEventDao.selectLatestByJobId(jobId);
        if (latestEvent == null) {
            return false;
        }

        return (now - latestEvent.getTriggerTime()) < quietMs;
    }
}
