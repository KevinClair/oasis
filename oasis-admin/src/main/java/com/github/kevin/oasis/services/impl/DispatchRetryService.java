package com.github.kevin.oasis.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.DispatchQueueDao;
import com.github.kevin.oasis.dao.JobFireLogDao;
import com.github.kevin.oasis.dao.JobInfoDao;
import com.github.kevin.oasis.models.entity.DispatchQueue;
import com.github.kevin.oasis.models.entity.JobFireLog;
import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.vo.schedule.DispatchRetryPayload;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * dispatch_queue 异步重试处理器。
 * 负责失败下发的异步重试与最终失败补偿。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchRetryService {

    private final SchedulerRuntimeProperties runtimeProperties;
    private final DispatchQueueDao dispatchQueueDao;
    private final JobInfoDao jobInfoDao;
    private final JobFireLogDao jobFireLogDao;
    private final ExecutorDispatchService executorDispatchService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.dispatch-retry-interval-ms:1000}")
    public void retryDispatchQueue() {
        if (!runtimeProperties.isEnabled() || !runtimeProperties.isDispatchRetryEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        // 节点异常退出后，PROCESSING 记录会悬挂；这里做超时恢复，避免任务永久卡死。
        long stuckBefore = now - runtimeProperties.getDispatchRetryMaxBackoffMs();
        dispatchQueueDao.recoverStuckProcessing(stuckBefore, now);

        List<DispatchQueue> dueRecords = dispatchQueueDao.selectDueRecords(now, runtimeProperties.getDispatchRetryBatchSize());
        if (dueRecords == null || dueRecords.isEmpty()) {
            return;
        }

        for (DispatchQueue queue : dueRecords) {
            processSingleRecord(queue, now);
        }
    }

    private void processSingleRecord(DispatchQueue queue, long now) {
        try {
            // 先抢占队列记录，避免多线程/多节点重复消费。
            if (dispatchQueueDao.claimProcessing(queue.getId()) <= 0) {
                return;
            }

            DispatchRetryPayload payload = parsePayload(queue.getPayload());
            JobInfo jobInfo = jobInfoDao.selectById(queue.getJobId());
            if (jobInfo == null) {
                markDeadWithCompensation(queue, payload, "任务不存在");
                jobFireLogDao.updateDispatch(JobFireLog.builder()
                        .id(queue.getFireLogId())
                        .status("FAILED")
                        .attemptNo(queue.getRetryCount() + 1)
                        .errorMessage("任务不存在")
                        .finishTime(System.currentTimeMillis())
                        .build());
                return;
            }

            int attemptNo = queue.getRetryCount() + 2;
            DispatchResult result = executorDispatchService.dispatch(jobInfo, queue.getFireLogId(), attemptNo, payload.getTriggerParam());

            if (result.isSuccess()) {
                onRetrySuccess(queue, attemptNo, result);
                return;
            }

            onRetryFail(queue, payload, now, attemptNo, result.getErrorMessage());
        } catch (Exception e) {
            log.error("dispatch retry process failed, queueId={}", queue.getId(), e);
        }
    }

    private void onRetrySuccess(DispatchQueue queue, int attemptNo, DispatchResult result) {
        dispatchQueueDao.markSuccess(queue.getId());
        jobFireLogDao.updateDispatch(JobFireLog.builder()
                .id(queue.getFireLogId())
                .status("RUNNING")
                .attemptNo(attemptNo)
                .executorAddress(result.getExecutorAddress())
                .errorMessage(result.getErrorMessage())
                .finishTime(null)
                .build());
    }

    private void onRetryFail(DispatchQueue queue, DispatchRetryPayload payload, long now, int attemptNo, String errorMessage) throws Exception {
        int maxRetry = payload.getMaxRetry() == null ? 0 : payload.getMaxRetry();
        int nextRetryCount = queue.getRetryCount() + 1;

        if (nextRetryCount > maxRetry) {
            markDeadWithCompensation(queue, payload, errorMessage);
            jobFireLogDao.updateDispatch(JobFireLog.builder()
                    .id(queue.getFireLogId())
                    .status("FAILED")
                    .attemptNo(attemptNo)
                    .errorMessage(errorMessage)
                    .finishTime(System.currentTimeMillis())
                    .build());
            return;
        }

        payload.setLastError(errorMessage);
        long nextRetryTime = now + calculateBackoff(nextRetryCount);
        dispatchQueueDao.reschedule(queue.getId(), nextRetryCount, nextRetryTime, objectMapper.writeValueAsString(payload));

        // 重试窗口内保持 PENDING，表示还有后续补偿机会。
        jobFireLogDao.updateDispatch(JobFireLog.builder()
                .id(queue.getFireLogId())
                .status("PENDING")
                .attemptNo(attemptNo)
                .errorMessage(errorMessage)
                .finishTime(null)
                .build());
    }

    private void markDeadWithCompensation(DispatchQueue queue, DispatchRetryPayload payload, String errorMessage) throws Exception {
        payload.setLastError(errorMessage);
        dispatchQueueDao.markDead(queue.getId(), objectMapper.writeValueAsString(payload));
    }

    private DispatchRetryPayload parsePayload(String payload) throws Exception {
        if (payload == null || payload.isBlank()) {
            return DispatchRetryPayload.builder().triggerParam(null).maxRetry(0).build();
        }
        return objectMapper.readValue(payload, DispatchRetryPayload.class);
    }

    private long calculateBackoff(int retryCount) {
        long base = runtimeProperties.getDispatchRetryBackoffMs();
        long max = runtimeProperties.getDispatchRetryMaxBackoffMs();
        long factor = 1L << Math.min(10, Math.max(0, retryCount - 1));
        long backoff = base * factor;
        return Math.min(backoff, max);
    }
}
