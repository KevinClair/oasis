package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.global.oauth.UserThreadLocal;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.base.UserInfo;
import com.github.kevin.oasis.models.vo.schedule.*;
import com.github.kevin.oasis.services.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 调度中心控制器
 */
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/job/list")
    @Permission
    public Response<JobListResponse> listJob(@RequestBody JobListRequest request) {
        return Response.success(scheduleService.getJobList(request));
    }

    @PostMapping("/job/save")
    @Permission
    public Response<Long> saveJob(@Valid @RequestBody JobSaveRequest request) {
        UserInfo userInfo = UserThreadLocal.getUserInfo();
        return Response.success(scheduleService.saveJob(request, userInfo.getUserId()));
    }

    @PostMapping("/job/enable")
    @Permission
    public Response<Integer> enableJob(@Valid @RequestBody JobEnableRequest request) {
        UserInfo userInfo = UserThreadLocal.getUserInfo();
        return Response.success(scheduleService.enableJobs(request, userInfo.getUserId()));
    }

    @PostMapping("/job/trigger")
    @Permission
    public Response<Long> triggerJob(@Valid @RequestBody JobTriggerRequest request) {
        UserInfo userInfo = UserThreadLocal.getUserInfo();
        return Response.success(scheduleService.triggerJob(request, userInfo.getUserId()));
    }

    @PostMapping("/log/list")
    @Permission
    public Response<JobLogListResponse> listLog(@RequestBody JobLogListRequest request) {
        return Response.success(scheduleService.getLogList(request));
    }

    @GetMapping("/log/{id}")
    @Permission
    public Response<JobLogVO> getLog(@PathVariable Long id) {
        return Response.success(scheduleService.getLogDetail(id));
    }

    @GetMapping("/app/{appCode}/alarm-template")
    @Permission
    public Response<AppAlarmTemplateVO> getAppAlarmTemplate(@PathVariable String appCode) {
        return Response.success(scheduleService.getAppAlarmTemplate(appCode));
    }

    @PostMapping("/app/alarm-template/save")
    @Permission
    public Response<Integer> saveAppAlarmTemplate(@Valid @RequestBody AppAlarmTemplateSaveRequest request) {
        return Response.success(scheduleService.saveAppAlarmTemplate(request));
    }

    @GetMapping("/job/{jobId}/alarm-policy")
    @Permission
    public Response<JobAlarmPolicyVO> getJobAlarmPolicy(@PathVariable Long jobId) {
        return Response.success(scheduleService.getJobAlarmPolicy(jobId));
    }

    @PostMapping("/job/alarm-policy/save")
    @Permission
    public Response<Integer> saveJobAlarmPolicy(@Valid @RequestBody JobAlarmPolicySaveRequest request) {
        return Response.success(scheduleService.saveJobAlarmPolicy(request));
    }

    @PostMapping("/job/{jobId}/alarm-events/list")
    @Permission
    public Response<JobAlarmEventListResponse> getJobAlarmEvents(
            @PathVariable Long jobId,
            @RequestBody JobAlarmEventListRequest request
    ) {
        return Response.success(scheduleService.getJobAlarmEvents(jobId, request));
    }

    @GetMapping("/alarm-event/{eventId}")
    @Permission
    public Response<JobAlarmEventDetailVO> getAlarmEvent(@PathVariable Long eventId) {
        return Response.success(scheduleService.getAlarmEventDetail(eventId));
    }
}
