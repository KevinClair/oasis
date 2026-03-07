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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 调度触发执行服务
 */
@Service
@RequiredArgsConstructor
public class JobTriggerExecutorServiceImpl implements JobTriggerExecutorService {

    private final JobFireLogDao jobFireLogDao;
    private final DispatchQueueDao dispatchQueueDao;
    private final ExecutorDispatchService executorDispatchService;
    private final SchedulerRuntimeProperties runtimeProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long trigger(JobInfo jobInfo, String triggerType, String triggerParam, Integer attemptNo) {
        int realAttempt = attemptNo == null || attemptNo <= 0 ? 1 : attemptNo;

        // 先落执行主日志，拿到 fireLogId 作为整条执行链路的主键。
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

        // 下发成功置 RUNNING；失败时按配置进入异步重试或直接失败补偿。
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
            // 入队失败时立即失败补偿，避免任务长期悬挂在 PENDING 状态。
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
