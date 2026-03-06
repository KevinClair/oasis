package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.dao.JobFireLogDao;
import com.github.kevin.oasis.models.entity.JobFireLog;
import com.github.kevin.oasis.models.entity.JobInfo;
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
    private final ExecutorDispatchService executorDispatchService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long trigger(JobInfo jobInfo, String triggerType, String triggerParam, Integer attemptNo) {
        int realAttempt = attemptNo == null || attemptNo <= 0 ? 1 : attemptNo;

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

        JobFireLog dispatchLog = JobFireLog.builder()
                .id(log.getId())
                .attemptNo(realAttempt)
                .executorAddress(dispatchResult.getExecutorAddress())
                .status(dispatchResult.isSuccess() ? "RUNNING" : "FAILED")
                .errorMessage(dispatchResult.getErrorMessage())
                .finishTime(dispatchResult.isSuccess() ? null : System.currentTimeMillis())
                .build();
        jobFireLogDao.updateDispatch(dispatchLog);

        return log.getId();
    }
}
