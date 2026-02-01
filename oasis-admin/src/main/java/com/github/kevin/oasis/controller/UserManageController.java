package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.systemManage.UserDeleteRequest;
import com.github.kevin.oasis.models.vo.systemManage.UserListRequest;
import com.github.kevin.oasis.models.vo.systemManage.UserListResponse;
import com.github.kevin.oasis.models.vo.systemManage.UserToggleStatusRequest;
import com.github.kevin.oasis.services.UserManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/systemManage/user")
@RequiredArgsConstructor
@Slf4j
public class UserManageController {

    private final UserManageService userManageService;

    /**
     * 获取用户列表
     *
     * @param request 查询参数
     * @return 用户列表响应
     */
    @PostMapping("/getUserList")
    @Permission
    public Response<UserListResponse> getUserList(@RequestBody UserListRequest request) {
        log.info("收到获取用户列表请求，参数：{}", request);

        UserListResponse response = userManageService.getUserList(request);

        return Response.success(response);
    }

    /**
     * 删除用户（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 成功响应
     */
    @PostMapping("/deleteUsers")
    @Permission
    public Response<Integer> deleteUsers(@Valid @RequestBody UserDeleteRequest request) {
        log.info("收到删除用户请求，参数：{}", request);

        int deletedCount = userManageService.deleteUsers(request);

        return Response.success(deletedCount);
    }

    /**
     * 切换用户状态（启用/禁用）
     *
     * @param request 切换状态请求参数
     * @return 成功响应
     */
    @PostMapping("/toggleUserStatus")
    @Permission
    public Response<Integer> toggleUserStatus(@Valid @RequestBody UserToggleStatusRequest request) {
        log.info("收到切换用户状态请求，参数：{}", request);

        int updatedCount = userManageService.toggleUserStatus(request);

        return Response.success(updatedCount);
    }
}

