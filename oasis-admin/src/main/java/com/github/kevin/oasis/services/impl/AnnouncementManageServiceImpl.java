package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.AnnouncementDao;
import com.github.kevin.oasis.models.entity.Announcement;
import com.github.kevin.oasis.models.vo.systemManage.*;
import com.github.kevin.oasis.services.AnnouncementManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementManageServiceImpl implements AnnouncementManageService {

    private final AnnouncementDao announcementDao;

    @Override
    public AnnouncementListResponse getAnnouncementList(AnnouncementListRequest request) {
        log.info("查询公告列表，参数：{}", request);

        // 计算分页参数
        int offset = 0;
        if (request.getCurrent() != null && request.getSize() != null) {
            offset = (request.getCurrent() - 1) * request.getSize();
        }
        request.setCurrent(offset);

        // 查询公告列表
        List<Announcement> announcementList = announcementDao.selectAnnouncementList(request);

        // 查询总数
        Long total = announcementDao.countAnnouncementList(request);

        // 转换为VO
        List<AnnouncementVO> announcementVOList = announcementList.stream().map(announcement ->
                AnnouncementVO.builder()
                        .id(announcement.getId())
                        .title(announcement.getTitle())
                        .content(announcement.getContent())
                        .type(announcement.getType())
                        .createBy(announcement.getCreateBy())
                        .createTime(announcement.getCreateTime())
                        .updateBy(announcement.getUpdateBy())
                        .updateTime(announcement.getUpdateTime())
                        .build()
        ).collect(Collectors.toList());

        // 构建响应
        AnnouncementListResponse response = AnnouncementListResponse.builder()
                .current(request.getCurrent() / request.getSize() + 1)
                .size(request.getSize())
                .total(total)
                .records(announcementVOList)
                .build();

        log.info("查询公告列表成功，总数：{}，当前页：{}，每页大小：{}", total, response.getCurrent(), response.getSize());

        return response;
    }

    @Override
    public Long saveAnnouncement(AnnouncementSaveRequest request) {
        log.info("保存公告，参数：{}", request);

        if (request.getId() == null) {
            // 新增
            Announcement announcement = Announcement.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .type(request.getType())
                    .createBy("admin") // TODO: 从当前登录用户获取
                    .updateBy("admin")
                    .build();

            announcementDao.insert(announcement);
            log.info("新增公告成功，公告ID：{}", announcement.getId());
            return announcement.getId();
        } else {
            // 编辑
            Announcement announcement = announcementDao.selectById(request.getId());
            if (announcement == null) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "公告不存在");
            }

            announcement.setTitle(request.getTitle());
            announcement.setContent(request.getContent());
            announcement.setType(request.getType());
            announcement.setUpdateBy("admin"); // TODO: 从当前登录用户获取

            announcementDao.update(announcement);
            log.info("更新公告成功，公告ID：{}", announcement.getId());
            return announcement.getId();
        }
    }

    @Override
    public AnnouncementVO getAnnouncementById(Long id) {
        log.info("查询公告详情，公告ID：{}", id);

        Announcement announcement = announcementDao.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "公告不存在");
        }

        AnnouncementVO announcementVO = AnnouncementVO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType())
                .createBy(announcement.getCreateBy())
                .createTime(announcement.getCreateTime())
                .updateBy(announcement.getUpdateBy())
                .updateTime(announcement.getUpdateTime())
                .build();

        log.info("查询公告详情成功");

        return announcementVO;
    }

    @Override
    public AnnouncementVO getLatestAnnouncement() {
        log.info("获取最新公告");

        Announcement announcement = announcementDao.selectLatestAnnouncement();
        if (announcement == null) {
            log.info("没有公告数据");
            return null;
        }

        AnnouncementVO announcementVO = AnnouncementVO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType())
                .createBy(announcement.getCreateBy())
                .createTime(announcement.getCreateTime())
                .updateBy(announcement.getUpdateBy())
                .updateTime(announcement.getUpdateTime())
                .build();

        log.info("获取最新公告成功，公告ID：{}", announcement.getId());

        return announcementVO;
    }

    @Override
    public int deleteAnnouncements(AnnouncementDeleteRequest request) {
        log.info("删除公告，参数：{}", request);

        if (request.getIds() == null || request.getIds().isEmpty()) {
            log.warn("删除公告失败：公告ID列表为空");
            return 0;
        }

        // 批量删除公告
        int deletedCount = announcementDao.deleteAnnouncementsByIds(request.getIds());

        log.info("删除公告成功，删除数量：{}", deletedCount);

        return deletedCount;
    }
}

