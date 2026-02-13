package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.systemManage.*;

/**
 * 公告管理服务接口
 */
public interface AnnouncementManageService {

    /**
     * 获取公告列表（分页）
     *
     * @param request 查询参数
     * @return 公告列表响应
     */
    AnnouncementListResponse getAnnouncementList(AnnouncementListRequest request);

    /**
     * 保存公告（新增/编辑）
     *
     * @param request 公告信息
     * @return 公告ID
     */
    Long saveAnnouncement(AnnouncementSaveRequest request);

    /**
     * 根据ID获取公告详情
     *
     * @param id 公告ID
     * @return 公告信息
     */
    AnnouncementVO getAnnouncementById(Long id);

    /**
     * 获取最新公告
     *
     * @return 最新公告信息，如果没有公告则返回null
     */
    AnnouncementVO getLatestAnnouncement();

    /**
     * 删除公告（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 删除的记录数
     */
    int deleteAnnouncements(AnnouncementDeleteRequest request);
}

