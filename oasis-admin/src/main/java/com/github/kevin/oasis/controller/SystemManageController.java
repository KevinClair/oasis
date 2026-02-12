package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.services.MenuManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统管理通用控制器
 */
@RestController
@RequestMapping("/systemManage")
@RequiredArgsConstructor
@Slf4j
public class SystemManageController {

    private final MenuManageService menuManageService;

    /**
     * 获取所有菜单的路由路径（平铺）
     *
     * @return 路由路径列表
     */
    @GetMapping("/getAllPages")
    @Permission
    public Response<List<String>> getAllPages() {
        log.info("收到获取所有菜单路由路径请求");

        List<String> pages = menuManageService.getAllPages();

        return Response.success(pages);
    }
}

