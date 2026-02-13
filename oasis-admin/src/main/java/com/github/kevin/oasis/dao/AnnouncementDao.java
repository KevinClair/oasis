package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.Announcement;
import com.github.kevin.oasis.models.vo.systemManage.AnnouncementListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告数据访问层
 */
@Mapper
public interface AnnouncementDao {

    /**
     * 查询公告列表（分页）
     *
     * @param request 查询参数
     * @return 公告列表
     */
    List<Announcement> selectAnnouncementList(@Param("request") AnnouncementListRequest request);

    /**
     * 查询公告总数
     *
     * @param request 查询参数
     * @return 公告总数
     */
    Long countAnnouncementList(@Param("request") AnnouncementListRequest request);

    /**
     * 根据ID查询公告
     *
     * @param id 公告ID
     * @return 公告信息
     */
    Announcement selectById(@Param("id") Long id);

    /**
     * 查询最新公告（按创建时间倒序的第一条）
     *
     * @return 最新公告信息
     */
    Announcement selectLatestAnnouncement();

    /**
     * 插入公告
     *
     * @param announcement 公告信息
     * @return 影响行数
     */
    int insert(Announcement announcement);

    /**
     * 更新公告
     *
     * @param announcement 公告信息
     * @return 影响行数
     */
    int update(Announcement announcement);

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     * @return 影响行数
     */
    int deleteAnnouncementsByIds(@Param("ids") List<Long> ids);
}

