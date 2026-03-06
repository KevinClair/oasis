package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.schedule.*;

public interface ScheduleService {

    JobListResponse getJobList(JobListRequest request);

    Long saveJob(JobSaveRequest request, String currentUserId);

    int enableJobs(JobEnableRequest request, String currentUserId);

    Long triggerJob(JobTriggerRequest request, String currentUserId);

    JobLogListResponse getLogList(JobLogListRequest request);

    JobLogVO getLogDetail(Long id);

    AppAlarmTemplateVO getAppAlarmTemplate(String appCode);

    int saveAppAlarmTemplate(AppAlarmTemplateSaveRequest request);

    JobAlarmPolicyVO getJobAlarmPolicy(Long jobId);

    int saveJobAlarmPolicy(JobAlarmPolicySaveRequest request);

    JobAlarmEventListResponse getJobAlarmEvents(Long jobId, JobAlarmEventListRequest request);

    JobAlarmEventDetailVO getAlarmEventDetail(Long eventId);
}
