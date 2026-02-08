package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.systemManage.*;
import com.github.kevin.oasis.services.RoleManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/systemManage/role")
@RequiredArgsConstructor
@Slf4j
public class RoleManageController {

    private final RoleManageService roleManageService;

    /**
     * 获取角色列表
     *
     * @param request 查询参数
     * @return 角色列表响应
     */
    @PostMapping("/getRoleList")
    @Permission
    public Response<RoleListResponse> getRoleList(@RequestBody RoleListRequest request) {
        log.info("收到获取角色列表请求，参数：{}", request);

        RoleListResponse response = roleManageService.getRoleList(request);

        return Response.success(response);
    }

    /**
     * 保存角色（新增/编辑）
     *
     * @param request 角色信息
     * @return 成功响应
     */
    @PostMapping("/saveRole")
    @Permission
    public Response<Long> saveRole(@Valid @RequestBody RoleSaveRequest request) {
        log.info("收到保存角色请求，参数：{}", request);

        Long roleId = roleManageService.saveRole(request);

        return Response.success(roleId);
    }

    /**
     * 删除角色（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 成功响应
     */
    @PostMapping("/deleteRoles")
    @Permission
    public Response<Integer> deleteRoles(@Valid @RequestBody RoleDeleteRequest request) {
        log.info("收到删除角色请求，参数：{}", request);

        int deletedCount = roleManageService.deleteRoles(request);

        return Response.success(deletedCount);
    }

    /**
     * 切换角色状态（启用/禁用）
     *
     * @param request 切换状态请求参数
     * @return 成功响应
     */
    @PostMapping("/toggleRoleStatus")
    @Permission
    public Response<Integer> toggleRoleStatus(@Valid @RequestBody RoleToggleStatusRequest request) {
        log.info("收到切换角色状态请求，参数：{}", request);

        int updatedCount = roleManageService.toggleRoleStatus(request);

        return Response.success(updatedCount);
    }

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/getRoleById/{id}")
    @Permission
    public Response<RoleVO> getRoleById(@PathVariable Long id) {
        log.info("收到获取角色详情请求，角色ID：{}", id);

        RoleVO roleVO = roleManageService.getRoleById(id);

        return Response.success(roleVO);
    }

    /**
     * 获取所有启用的角色（用于下拉选择）
     *
     * @return 角色列表
     */
    @GetMapping("/getAllEnabledRoles")
    @Permission
    public Response<List<RoleVO>> getAllEnabledRoles() {
        log.info("收到获取所有启用角色请求");

        List<RoleVO> roles = roleManageService.getAllEnabledRoles();

        return Response.success(roles);
    }
}

