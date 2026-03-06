package com.github.kevin.oasis.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.*;
import com.github.kevin.oasis.models.entity.*;
import com.github.kevin.oasis.models.vo.schedule.*;
import com.github.kevin.oasis.services.JobTriggerExecutorService;
import com.github.kevin.oasis.services.ScheduleService;
import com.github.kevin.oasis.utils.ScheduleNextTimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 调度服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private static final int SHARD_COUNT = 128;

    private final JobInfoDao jobInfoDao;
    private final JobScheduleDao jobScheduleDao;
    private final JobFireLogDao jobFireLogDao;
    private final AppAlarmTemplateDao appAlarmTemplateDao;
    private final JobAlarmPolicyDao jobAlarmPolicyDao;
    private final JobAlarmEventDao jobAlarmEventDao;
    private final JobAlarmNotifyRecordDao jobAlarmNotifyRecordDao;
    private final ApplicationDao applicationDao;
    private final JobTriggerExecutorService jobTriggerExecutorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JobListResponse getJobList(JobListRequest request) {
        List<JobInfo> list = jobInfoDao.selectJobList(request);
        Long total = jobInfoDao.countJobList(request);

        List<JobVO> records = list.stream().map(this::toJobVO).collect(Collectors.toList());

        return JobListResponse.builder()
                .records(records)
                .current(request.getCurrent())
                .size(request.getSize())
                .total(total)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveJob(JobSaveRequest request, String currentUserId) {
        Application app = applicationDao.selectByAppCode(request.getAppCode());
        if (app == null || Boolean.FALSE.equals(app.getStatus())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用不存在或已禁用，无法创建任务");
        }

        if (request.getId() == null) {
            JobInfo jobInfo = JobInfo.builder()
                    .appCode(request.getAppCode())
                    .jobName(request.getJobName())
                    .handlerName(request.getHandlerName())
                    .scheduleType(request.getScheduleType())
                    .scheduleConf(request.getScheduleConf())
                    .routeStrategy(defaultString(request.getRouteStrategy(), "ROUND"))
                    .blockStrategy(defaultString(request.getBlockStrategy(), "SERIAL"))
                    .retryCount(defaultNumber(request.getRetryCount(), 0))
                    .timeoutSeconds(defaultNumber(request.getTimeoutSeconds(), 30))
                    .status(request.getStatus())
                    .alarmInheritApp(request.getAlarmInheritApp() == null || request.getAlarmInheritApp())
                    .createBy(currentUserId)
                    .updateBy(currentUserId)
                    .build();
            jobInfoDao.insert(jobInfo);

            JobSchedule schedule = JobSchedule.builder()
                    .jobId(jobInfo.getId())
                    .shardId((int) (jobInfo.getId() % SHARD_COUNT))
                    .nextTriggerTime(computeInitialNextTriggerTime(jobInfo.getScheduleType(), jobInfo.getScheduleConf()))
                    .misfireStrategy("FIRE_ONCE")
                    .triggerStatus(jobInfo.getStatus())
                    .version(0L)
                    .build();
            jobScheduleDao.insert(schedule);

            return jobInfo.getId();
        }

        JobInfo exist = jobInfoDao.selectById(request.getId());
        if (exist == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "任务不存在");
        }

        exist.setAppCode(request.getAppCode());
        exist.setJobName(request.getJobName());
        exist.setHandlerName(request.getHandlerName());
        exist.setScheduleType(request.getScheduleType());
        exist.setScheduleConf(request.getScheduleConf());
        exist.setRouteStrategy(defaultString(request.getRouteStrategy(), "ROUND"));
        exist.setBlockStrategy(defaultString(request.getBlockStrategy(), "SERIAL"));
        exist.setRetryCount(defaultNumber(request.getRetryCount(), 0));
        exist.setTimeoutSeconds(defaultNumber(request.getTimeoutSeconds(), 30));
        exist.setStatus(request.getStatus());
        exist.setAlarmInheritApp(request.getAlarmInheritApp() == null || request.getAlarmInheritApp());
        exist.setUpdateBy(currentUserId);
        jobInfoDao.update(exist);

        JobSchedule schedule = jobScheduleDao.selectByJobId(exist.getId());
        if (schedule == null) {
            schedule = JobSchedule.builder()
                    .jobId(exist.getId())
                    .shardId((int) (exist.getId() % SHARD_COUNT))
                    .nextTriggerTime(computeInitialNextTriggerTime(exist.getScheduleType(), exist.getScheduleConf()))
                    .misfireStrategy("FIRE_ONCE")
                    .triggerStatus(exist.getStatus())
                    .version(0L)
                    .build();
            jobScheduleDao.insert(schedule);
        } else {
            schedule.setTriggerStatus(exist.getStatus());
            schedule.setNextTriggerTime(computeInitialNextTriggerTime(exist.getScheduleType(), exist.getScheduleConf()));
            jobScheduleDao.updateByJobId(schedule);
        }

        return exist.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enableJobs(JobEnableRequest request, String currentUserId) {
        int updated = jobInfoDao.updateStatusByIds(request.getIds(), request.getStatus(), currentUserId);
        jobScheduleDao.updateTriggerStatusByJobIds(request.getIds(), request.getStatus());
        return updated;
    }

    @Override
    public Long triggerJob(JobTriggerRequest request, String currentUserId) {
        JobInfo jobInfo = jobInfoDao.selectById(request.getJobId());
        if (jobInfo == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "任务不存在");
        }
        if (Boolean.FALSE.equals(jobInfo.getStatus())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "任务未启用");
        }
        return jobTriggerExecutorService.trigger(jobInfo, "MANUAL", request.getTriggerParam(), 1);
    }

    @Override
    public JobLogListResponse getLogList(JobLogListRequest request) {
        List<JobLogVO> records = jobFireLogDao.selectLogList(request);
        Long total = jobFireLogDao.countLogList(request);
        return JobLogListResponse.builder()
                .records(records)
                .current(request.getCurrent())
                .size(request.getSize())
                .total(total)
                .build();
    }

    @Override
    public JobLogVO getLogDetail(Long id) {
        JobLogVO vo = jobFireLogDao.selectLogDetailById(id);
        if (vo == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "日志不存在");
        }
        return vo;
    }

    @Override
    public AppAlarmTemplateVO getAppAlarmTemplate(String appCode) {
        AppAlarmTemplate template = appAlarmTemplateDao.selectByAppCode(appCode);
        if (template == null) {
            return AppAlarmTemplateVO.builder()
                    .appCode(appCode)
                    .receivers(new ArrayList<>())
                    .channels(new ArrayList<>())
                    .quietPeriodMinutes(10)
                    .failThreshold(1)
                    .timeoutSeconds(30)
                    .enabled(true)
                    .build();
        }
        return toAppAlarmTemplateVO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveAppAlarmTemplate(AppAlarmTemplateSaveRequest request) {
        Application app = applicationDao.selectByAppCode(request.getAppCode());
        if (app == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用不存在");
        }

        AppAlarmTemplate exist = appAlarmTemplateDao.selectByAppCode(request.getAppCode());
        AppAlarmTemplate data = AppAlarmTemplate.builder()
                .appCode(request.getAppCode())
                .receivers(toJson(request.getReceivers()))
                .channels(toJson(request.getChannels()))
                .quietPeriodMinutes(defaultNumber(request.getQuietPeriodMinutes(), 10))
                .failThreshold(defaultNumber(request.getFailThreshold(), 1))
                .timeoutSeconds(defaultNumber(request.getTimeoutSeconds(), 30))
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build();

        if (exist == null) {
            return appAlarmTemplateDao.insert(data);
        }
        return appAlarmTemplateDao.updateByAppCode(data);
    }

    @Override
    public JobAlarmPolicyVO getJobAlarmPolicy(Long jobId) {
        JobAlarmPolicy policy = jobAlarmPolicyDao.selectByJobId(jobId);
        if (policy == null) {
            return JobAlarmPolicyVO.builder()
                    .jobId(jobId)
                    .inheritAppTemplate(true)
                    .receivers(new ArrayList<>())
                    .channels(new ArrayList<>())
                    .enabled(true)
                    .build();
        }

        return toJobAlarmPolicyVO(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveJobAlarmPolicy(JobAlarmPolicySaveRequest request) {
        JobInfo jobInfo = jobInfoDao.selectById(request.getJobId());
        if (jobInfo == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "任务不存在");
        }

        JobAlarmPolicy exist = jobAlarmPolicyDao.selectByJobId(request.getJobId());
        JobAlarmPolicy data = JobAlarmPolicy.builder()
                .jobId(request.getJobId())
                .inheritAppTemplate(request.getInheritAppTemplate() == null || request.getInheritAppTemplate())
                .receivers(toJson(request.getReceivers()))
                .channels(toJson(request.getChannels()))
                .quietPeriodMinutes(request.getQuietPeriodMinutes())
                .failThreshold(request.getFailThreshold())
                .timeoutSeconds(request.getTimeoutSeconds())
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build();

        if (exist == null) {
            return jobAlarmPolicyDao.insert(data);
        }
        return jobAlarmPolicyDao.updateByJobId(data);
    }

    @Override
    public JobAlarmEventListResponse getJobAlarmEvents(Long jobId, JobAlarmEventListRequest request) {
        List<JobAlarmEventVO> records = jobAlarmEventDao.selectEventList(jobId, request);
        Long total = jobAlarmEventDao.countEventList(jobId, request);

        return JobAlarmEventListResponse.builder()
                .records(records)
                .current(request.getCurrent())
                .size(request.getSize())
                .total(total)
                .build();
    }

    @Override
    public JobAlarmEventDetailVO getAlarmEventDetail(Long eventId) {
        JobAlarmEvent event = jobAlarmEventDao.selectById(eventId);
        if (event == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "告警事件不存在");
        }

        List<JobAlarmNotifyRecord> notifyRecords = jobAlarmNotifyRecordDao.selectByAlarmEventId(eventId);
        List<AlarmNotifyRecordVO> recordVOList = notifyRecords.stream().map(item -> AlarmNotifyRecordVO.builder()
                .id(item.getId())
                .alarmEventId(item.getAlarmEventId())
                .channel(item.getChannel())
                .receiver(item.getReceiver())
                .sendStatus(item.getSendStatus())
                .responseMessage(item.getResponseMessage())
                .sendTime(item.getSendTime())
                .build()).collect(Collectors.toList());

        return JobAlarmEventDetailVO.builder()
                .id(event.getId())
                .jobId(event.getJobId())
                .fireLogId(event.getFireLogId())
                .alarmType(event.getAlarmType())
                .alarmContent(event.getAlarmContent())
                .notifyStatus(event.getNotifyStatus())
                .triggerTime(event.getTriggerTime())
                .notifyRecords(recordVOList)
                .build();
    }

    private JobVO toJobVO(JobInfo info) {
        JobSchedule schedule = jobScheduleDao.selectByJobId(info.getId());
        return JobVO.builder()
                .id(info.getId())
                .appCode(info.getAppCode())
                .jobName(info.getJobName())
                .handlerName(info.getHandlerName())
                .scheduleType(info.getScheduleType())
                .scheduleConf(info.getScheduleConf())
                .routeStrategy(info.getRouteStrategy())
                .blockStrategy(info.getBlockStrategy())
                .retryCount(info.getRetryCount())
                .timeoutSeconds(info.getTimeoutSeconds())
                .status(info.getStatus())
                .alarmInheritApp(info.getAlarmInheritApp())
                .nextTriggerTime(schedule != null ? schedule.getNextTriggerTime() : null)
                .createBy(info.getCreateBy())
                .createTime(info.getCreateTime())
                .updateBy(info.getUpdateBy())
                .updateTime(info.getUpdateTime())
                .build();
    }

    private AppAlarmTemplateVO toAppAlarmTemplateVO(AppAlarmTemplate template) {
        return AppAlarmTemplateVO.builder()
                .id(template.getId())
                .appCode(template.getAppCode())
                .receivers(parseJsonList(template.getReceivers()))
                .channels(parseJsonList(template.getChannels()))
                .quietPeriodMinutes(template.getQuietPeriodMinutes())
                .failThreshold(template.getFailThreshold())
                .timeoutSeconds(template.getTimeoutSeconds())
                .enabled(template.getEnabled())
                .build();
    }

    private JobAlarmPolicyVO toJobAlarmPolicyVO(JobAlarmPolicy policy) {
        return JobAlarmPolicyVO.builder()
                .id(policy.getId())
                .jobId(policy.getJobId())
                .inheritAppTemplate(policy.getInheritAppTemplate())
                .receivers(parseJsonList(policy.getReceivers()))
                .channels(parseJsonList(policy.getChannels()))
                .quietPeriodMinutes(policy.getQuietPeriodMinutes())
                .failThreshold(policy.getFailThreshold())
                .timeoutSeconds(policy.getTimeoutSeconds())
                .enabled(policy.getEnabled())
                .build();
    }

    private String toJson(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? new ArrayList<>() : value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "序列化告警配置失败");
        }
    }

    private List<String> parseJsonList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("告警配置反序列化失败，value={}", value);
            return new ArrayList<>();
        }
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private Integer defaultNumber(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Long computeInitialNextTriggerTime(String scheduleType, String scheduleConf) {
        Long now = System.currentTimeMillis();
        Long next = ScheduleNextTimeCalculator.computeNextTriggerTime(scheduleType, scheduleConf, now);
        return next == null ? now + 60_000L : next;
    }
}
