package com.github.kevin.oasis.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.DispatchQueueDao;
import com.github.kevin.oasis.dao.JobFireLogDao;
import com.github.kevin.oasis.models.entity.DispatchQueue;
import com.github.kevin.oasis.models.entity.JobFireLog;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.vo.schedule.DispatchRetryPayload;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorDispatchService;
import com.github.kevin.oasis.services.JobTriggerExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 调度触发执行服务。
 * 集成阻塞策略：COVER_EARLY / DISCARD_LATER / SERIAL_EXECUTION。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobTriggerExecutorServiceImpl implements JobTriggerExecutorService {

    private static final String BLOCK_DISCARD = "DISCARD_LATER";
    private static final String BLOCK_COVER = "COVER_EARLY";

    private final JobFireLogDao jobFireLogDao;
    private final DispatchQueueDao dispatchQueueDao;
    private final ExecutorDispatchService executorDispatchService;
    private final SchedulerRuntimeProperties runtimeProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long trigger(JobInfo jobInfo, String triggerType, String triggerParam, Integer attemptNo) {
        int realAttempt = attemptNo == null || attemptNo <= 0 ? 1 : attemptNo;

        // 阻塞策略检查：仅在非重试 attempt 时检查（retry 由 DispatchRetryService 驱动，已持有 fireLog）
        if (realAttempt == 1) {
            Long blocked = applyBlockStrategy(jobInfo);
            if (blocked == null) {
                // DISCARD_LATER：跳过本次触发
                return null;
            }
        }

        // 先落执行主日志
        JobFireLog log = JobFireLog.builder()
                .jobId(jobInfo.getId())
                .triggerTime(System.currentTimeMillis())
                .triggerType(triggerType)
                .status("PENDING")
                .attemptNo(realAttempt)
                .traceId(UUID.randomUUID().toString().replace("-", ""))
                .build();
        jobFireLogDao.insert(log);

        DispatchResult dispatchResult = executorDispatchService.dispatch(jobInfo, log.getId(), realAttempt, triggerParam);
        boolean enqueueRetry = shouldEnqueueRetry(jobInfo, dispatchResult);

        JobFireLog dispatchLog = JobFireLog.builder()
                .id(log.getId())
                .attemptNo(realAttempt)
                .executorAddress(dispatchResult.getExecutorAddress())
                .status(dispatchResult.isSuccess() ? "RUNNING" : (enqueueRetry ? "PENDING" : "FAILED"))
                .errorMessage(dispatchResult.getErrorMessage())
                .finishTime(dispatchResult.isSuccess() || enqueueRetry ? null : System.currentTimeMillis())
                .build();
        jobFireLogDao.updateDispatch(dispatchLog);

        if (enqueueRetry) {
            enqueueRetryRecord(jobInfo, log.getId(), realAttempt, dispatchResult, triggerParam);
        }

        return log.getId();
    }

    /**
     * 阻塞策略处理。
     *
     * @return null 表示 DISCARD_LATER（跳过本次），非 null 表示可继续触发
     */
    private Long applyBlockStrategy(JobInfo jobInfo) {
        String blockStrategy = jobInfo.getBlockStrategy();
        if (blockStrategy == null || blockStrategy.isBlank()) {
            return jobInfo.getId(); // 无阻塞策略，直接放行
        }

        List<JobFireLog> runningLogs = jobFireLogDao.selectRunningByJobId(jobInfo.getId());
        if (runningLogs == null || runningLogs.isEmpty()) {
            return jobInfo.getId(); // 无运行中任务，放行
        }

        String strategy = blockStrategy.trim().toUpperCase();

        if (BLOCK_DISCARD.equals(strategy)) {
            log.info("block strategy DISCARD_LATER: skip trigger, jobId={}, runningLogs={}", jobInfo.getId(), runningLogs.size());
            return null;
        }

        if (BLOCK_COVER.equals(strategy)) {
            int cancelled = jobFireLogDao.cancelRunningByJobId(jobInfo.getId());
            log.info("block strategy COVER_EARLY: cancelled {} running logs, jobId={}", cancelled, jobInfo.getId());
            return jobInfo.getId();
        }

        // SERIAL_EXECUTION 及其他：默认跳过
        log.info("block strategy {}: skip trigger, jobId={}, runningLogs={}", strategy, jobInfo.getId(), runningLogs.size());
        return null;
    }

    private boolean shouldEnqueueRetry(JobInfo jobInfo, DispatchResult dispatchResult) {
        if (dispatchResult.isSuccess()) {
            return false;
        }
        if (!runtimeProperties.isDispatchRetryEnabled()) {
            return false;
        }
        return jobInfo.getRetryCount() != null && jobInfo.getRetryCount() > 0;
    }

    private void enqueueRetryRecord(JobInfo jobInfo, Long fireLogId, Integer attemptNo, DispatchResult dispatchResult, String triggerParam) {
        try {
            DispatchRetryPayload payload = DispatchRetryPayload.builder()
                    .triggerParam(triggerParam)
                    .maxRetry(jobInfo.getRetryCount())
                    .lastError(dispatchResult.getErrorMessage())
                    .build();

            dispatchQueueDao.insert(DispatchQueue.builder()
                    .fireLogId(fireLogId)
                    .jobId(jobInfo.getId())
                    .targetAddress(dispatchResult.getExecutorAddress())
                    .payload(objectMapper.writeValueAsString(payload))
                    .status("PENDING")
                    .retryCount(0)
                    .nextRetryTime(System.currentTimeMillis() + runtimeProperties.getDispatchRetryBackoffMs())
                    .build());
        } catch (Exception e) {
            jobFireLogDao.updateDispatch(JobFireLog.builder()
                    .id(fireLogId)
                    .status("FAILED")
                    .attemptNo(attemptNo)
                    .errorMessage("重试入队失败:" + e.getMessage())
                    .finishTime(System.currentTimeMillis())
                    .build());
        }
    }
}
