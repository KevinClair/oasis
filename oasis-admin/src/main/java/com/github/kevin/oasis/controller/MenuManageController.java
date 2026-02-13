package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.systemManage.*;
import com.github.kevin.oasis.services.MenuManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/systemManage/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuManageController {

    private final MenuManageService menuManageService;

    /**
     * 获取菜单列表（树形结构）
     *
     * @param constant 常量数据筛选（可选）：null-全部，true-仅常量路由，false-仅动态路由
     * @param status 状态筛选（可选）：null-全部，true-仅启用，false-仅禁用
     * @return 菜单列表响应
     */
    @GetMapping("/getMenuList")
    @Permission
    public Response<MenuListResponse> getMenuList(
            @RequestParam(required = false) Boolean constant,
            @RequestParam(required = false) Boolean status) {
        log.info("收到获取菜单列表请求，常量筛选：{}，状态筛选：{}", constant, status);

        MenuListRequest request = MenuListRequest.builder()
                .constant(constant)
                .status(status)
                .build();

        MenuListResponse response = menuManageService.getMenuList(request);

        return Response.success(response);
    }

    /**
     * 保存菜单（新增/编辑）
     *
     * @param request 菜单信息
     * @return 成功响应
     */
    @PostMapping("/saveMenu")
    @Permission
    public Response<Long> saveMenu(@Valid @RequestBody MenuSaveRequest request) {
        log.info("收到保存菜单请求，参数：{}", request);

        Long menuId = menuManageService.saveMenu(request);

        return Response.success(menuId);
    }

    /**
     * 根据ID获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    @GetMapping("/getMenuById/{id}")
    @Permission
    public Response<MenuVO> getMenuById(@PathVariable Long id) {
        log.info("收到获取菜单详情请求，菜单ID：{}", id);

        MenuVO menuVO = menuManageService.getMenuById(id);

        return Response.success(menuVO);
    }

    /**
     * 删除菜单（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 成功响应
     */
    @PostMapping("/deleteMenus")
    @Permission
    public Response<Integer> deleteMenus(@Valid @RequestBody MenuDeleteRequest request) {
        log.info("收到删除菜单请求，参数：{}", request);

        int deletedCount = menuManageService.deleteMenus(request);

        return Response.success(deletedCount);
    }
}

