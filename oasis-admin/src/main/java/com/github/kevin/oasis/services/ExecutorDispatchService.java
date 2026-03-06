package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.entity.JobInfo;

public interface ExecutorDispatchService {

    DispatchResult dispatch(JobInfo jobInfo, Long fireLogId, Integer attemptNo, String triggerParam);
}
