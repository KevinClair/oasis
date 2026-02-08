package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.systemManage.*;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface RoleManageService {

    /**
     * 获取角色列表
     *
     * @param request 查询参数
     * @return 角色列表响应
     */
    RoleListResponse getRoleList(RoleListRequest request);

    /**
     * 保存角色（新增/编辑）
     *
     * @param request 角色信息
     * @return 角色ID
     */
    Long saveRole(RoleSaveRequest request);

    /**
     * 删除角色（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 删除的记录数
     */
    int deleteRoles(RoleDeleteRequest request);

    /**
     * 切换角色状态（启用/禁用）
     *
     * @param request 切换状态请求参数
     * @return 更新的记录数
     */
    int toggleRoleStatus(RoleToggleStatusRequest request);

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID
     * @return 角色信息
     */
    RoleVO getRoleById(Long id);

    /**
     * 获取所有启用的角色
     *
     * @return 角色列表
     */
    List<RoleVO> getAllEnabledRoles();
}

