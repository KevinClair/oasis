package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.entity.JobInfo;

public interface JobTriggerExecutorService {

    Long trigger(JobInfo jobInfo, String triggerType, String triggerParam, Integer attemptNo);
}
