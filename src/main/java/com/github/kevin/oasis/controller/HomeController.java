package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.home.GetAnnouncementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    /**
     * 获取公告信息
     *
     * @return
     */
    @GetMapping("/getAnnouncement")
    public Response<GetAnnouncementResponse> getAnnouncement(){
        return Response.success(GetAnnouncementResponse.builder().content("测试公告").title("公告").createTime("2025-10-10 00:00:00").build());
    }
}
