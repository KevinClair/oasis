package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.systemManage.*;

import java.util.List;

/**
 * 菜单管理服务接口
 */
public interface MenuManageService {

    /**
     * 获取菜单列表（树形结构）
     *
     * @return 菜单列表响应
     */
    MenuListResponse getMenuList();

    /**
     * 保存菜单（新增/编辑）
     *
     * @param request 菜单信息
     * @return 菜单ID
     */
    Long saveMenu(MenuSaveRequest request);

    /**
     * 根据ID获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    MenuVO getMenuById(Long id);

    /**
     * 删除菜单（支持批量删除）
     *
     * @param request 删除请求参数
     * @return 删除的记录数
     */
    int deleteMenus(MenuDeleteRequest request);

    /**
     * 获取所有菜单的路由路径（平铺）
     *
     * @return 路由路径列表
     */
    List<String> getAllPages();
}

