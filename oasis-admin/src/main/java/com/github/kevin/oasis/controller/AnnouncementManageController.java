package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.systemManage.*;
import com.github.kevin.oasis.services.AnnouncementManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/systemManage/announcement")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementManageController {

    private final AnnouncementManageService announcementManageService;

    /**
     * 获取公告列表（分页）
     *
     * @param request 查询参数
     * @return 公告列表响应
     */
    @PostMapping("/getAnnouncementList")
    @Permission
    public Response<AnnouncementListResponse> getAnnouncementList(@RequestBody AnnouncementListRequest request) {
        log.info("收到获取公告列表请求，参数：{}", request);

        AnnouncementListResponse response = announcementManageService.getAnnouncementList(request);

        return Response.success(response);
    }

    /**
     * 保存公告（新增/编辑）
     *
     * @param request 公告信息
     * @return 成功响应
     */
    @PostMapping("/saveAnnouncement")
    @Permission
    public Response<Long> saveAnnouncement(@Valid @RequestBody AnnouncementSaveRequest request) {
        log.info("收到保存公告请求，参数：{}", request);

        Long announcementId = announcementManageService.saveAnnouncement(request);

        return Response.success(announcementId);
    }

    /**
     * 根据ID获取公告详情
     *
     * @param id 公告ID
     * @return 公告信息
     */
    @GetMapping("/getAnnouncementById/{id}")
    @Permission
    public Response<AnnouncementVO> getAnnouncementById(@PathVariable Long id) {
        log.info("收到获取公告详情请求，公告ID：{}", id);

        AnnouncementVO announcementVO = announcementManageService.getAnnouncementById(id);

        return Response.success(announcementVO);
    }

    /**
     * 获取最新公告
     *
     * @return 最新公告信息
     */
    @GetMapping("/getLatestAnnouncement")
    public Response<AnnouncementVO> getLatestAnnouncement() {
        log.info("收到获取最新公告请求");

        AnnouncementVO announcementVO = announcementManageService.getLatestAnnouncement();

        return Response.success(announcementVO);
    }

    /**
     * 删除公告（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 成功响应
     */
    @PostMapping("/deleteAnnouncements")
    @Permission
    public Response<Integer> deleteAnnouncements(@Valid @RequestBody AnnouncementDeleteRequest request) {
        log.info("收到删除公告请求，参数：{}", request);

        int deletedCount = announcementManageService.deleteAnnouncements(request);

        return Response.success(deletedCount);
    }
}

