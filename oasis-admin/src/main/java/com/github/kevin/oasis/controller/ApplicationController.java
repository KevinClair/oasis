package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.global.oauth.UserThreadLocal;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.base.UserInfo;
import com.github.kevin.oasis.models.vo.application.*;
import com.github.kevin.oasis.services.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用管理控制器
 */
@RestController
@RequestMapping("/systemManage/application")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 获取应用列表
     *
     * @param request 查询参数
     * @return 应用列表响应
     */
    @PostMapping("/getApplicationList")
    @Permission
    public Response<ApplicationListResponse> getApplicationList(@RequestBody ApplicationListRequest request) {
        // 获取当前用户工号，用于数据权限过滤
        UserInfo currentUser = UserThreadLocal.getUserInfo();
        request.setCurrentUserId(currentUser.getUserId());

        log.info("收到获取应用列表请求，参数：{}", request);

        ApplicationListResponse response = applicationService.getApplicationList(request);

        return Response.success(response);
    }

    /**
     * 保存应用（新增/编辑）
     *
     * @param request 应用信息
     * @return 成功响应
     */
    @PostMapping("/saveApplication")
    @Permission
    public Response<Long> saveApplication(@Valid @RequestBody ApplicationSaveRequest request) {
        UserInfo currentUser = UserThreadLocal.getUserInfo();
        log.info("收到保存应用请求，参数：{}", request);

        Long applicationId = applicationService.saveApplication(request, currentUser.getUserId());

        return Response.success(applicationId);
    }

    /**
     * 根据ID获取应用详情
     *
     * @param id 应用ID
     * @return 应用信息
     */
    @GetMapping("/getApplicationById/{id}")
    @Permission
    public Response<ApplicationVO> getApplicationById(@PathVariable Long id) {
        log.info("收到获取应用详情请求，应用ID：{}", id);

        ApplicationVO applicationVO = applicationService.getApplicationById(id);

        return Response.success(applicationVO);
    }

    /**
     * 删除应用（批量支持）
     *
     * @param request 删除请求参数
     * @return 成功响应
     */
    @PostMapping("/deleteApplications")
    @Permission
    public Response<Integer> deleteApplications(@Valid @RequestBody ApplicationDeleteRequest request) {
        UserInfo currentUser = UserThreadLocal.getUserInfo();
        log.info("收到删除应用请求，参数：{}", request);

        int deletedCount = applicationService.deleteApplications(request, currentUser.getUserId());

        return Response.success(deletedCount);
    }

    /**
     * 获取应用注册节点列表
     *
     * @param appCode 应用Code
     * @return 注册节点列表
     */
    @GetMapping("/getRegistrationNodes/{appCode}")
    @Permission
    public Response<List<ApplicationRegistrationVO>> getRegistrationNodes(@PathVariable String appCode) {
        log.info("收到获取应用注册节点请求，应用Code：{}", appCode);

        List<ApplicationRegistrationVO> nodes = applicationService.getRegistrationNodes(appCode);

        return Response.success(nodes);
    }
}

