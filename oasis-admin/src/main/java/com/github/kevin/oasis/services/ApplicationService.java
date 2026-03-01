package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.application.*;

import java.util.List;

/**
 * 应用管理服务接口
 */
public interface ApplicationService {

    /**
     * 获取应用列表（带数据权限过滤）
     *
     * @param request 查询参数
     * @return 应用列表响应
     */
    ApplicationListResponse getApplicationList(ApplicationListRequest request);

    /**
     * 保存应用（新增/编辑）
     *
     * @param request 应用信息
     * @param currentUserId 当前用户工号
     * @return 应用ID
     */
    Long saveApplication(ApplicationSaveRequest request, String currentUserId);

    /**
     * 根据ID获取应用详情
     *
     * @param id 应用ID
     * @return 应用信息
     */
    ApplicationVO getApplicationById(Long id);

    /**
     * 删除应用（批量支持）
     *
     * @param request 删除请求参数
     * @param currentUserId 当前用户工号
     * @return 删除的记录数
     */
    int deleteApplications(ApplicationDeleteRequest request, String currentUserId);

    /**
     * 获取应用注册节点列表
     *
     * @param appCode 应用Code
     * @return 注册节点列表
     */
    List<ApplicationRegistrationVO> getRegistrationNodes(String appCode);
}

