package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.route.MenuRoute;
import com.github.kevin.oasis.models.vo.route.UserRouteResponse;

import java.util.List;

/**
 * 路由服务接口
 */
public interface RouteService {

    /**
     * 获取常量路由
     *
     * @return 常量路由列表
     */
    List<MenuRoute> getConstantRoutes();

    /**
     * 获取用户动态路由
     *
     * @param userId 用户工号
     * @return 用户路由响应
     */
    UserRouteResponse getUserRoutes(String userId);

    /**
     * 检查路由是否存在
     *
     * @param routeName 路由名称
     * @return 是否存在
     */
    boolean isRouteExist(String routeName);
}

