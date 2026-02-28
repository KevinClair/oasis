package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.systemManage.*;

/**
 * 用户管理服务接口
 */
public interface UserManageService {

    /**
     * 获取用户列表
     *
     * @param request 查询参数
     * @return 用户列表响应
     */
    UserListResponse getUserList(UserListRequest request);

    /**
     * 保存用户（新增/编辑）
     *
     * @param request 用户信息
     * @return 用户ID
     */
    Long saveUser(UserSaveRequest request);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 删除用户（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 删除的记录数
     */
    int deleteUsers(UserDeleteRequest request);

    /**
     * 切换用户状态（启用/禁用）
     *
     * @param request 切换状态请求参数
     * @return 更新的记录数
     */
    int toggleUserStatus(UserToggleStatusRequest request);

    /**
     * 重置用户密码（批量支持）
     *
     * @param request 重置密码请求参数
     * @return 更新的记录数
     */
    int resetPassword(UserDeleteRequest request);
}
