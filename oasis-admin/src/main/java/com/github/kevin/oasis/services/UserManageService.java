package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.systemManage.UserDeleteRequest;
import com.github.kevin.oasis.models.vo.systemManage.UserListRequest;
import com.github.kevin.oasis.models.vo.systemManage.UserListResponse;
import com.github.kevin.oasis.models.vo.systemManage.UserToggleStatusRequest;

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
}

