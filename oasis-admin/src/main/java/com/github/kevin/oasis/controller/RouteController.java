package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.global.oauth.UserThreadLocal;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.base.UserInfo;
import com.github.kevin.oasis.models.vo.route.MenuRoute;
import com.github.kevin.oasis.models.vo.route.UserRouteResponse;
import com.github.kevin.oasis.services.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 路由控制器
 */
@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
@Slf4j
public class RouteController {

    private final RouteService routeService;

    /**
     * 获取常量路由
     *
     * @return 常量路由列表
     */
    @GetMapping("/getConstantRoutes")
    public Response<List<MenuRoute>> getConstantRoutes() {
        log.info("收到获取常量路由请求");

        List<MenuRoute> routes = routeService.getConstantRoutes();

        return Response.success(routes);
    }

    /**
     * 获取用户动态路由
     *
     * @return 用户路由响应
     */
    @GetMapping("/getUserRoutes")
    @Permission
    public Response<UserRouteResponse> getUserRoutes() {
        // 从ThreadLocal中获取当前登录用户信息
        UserInfo currentUser = UserThreadLocal.getUserInfo();
        log.info("收到获取用户动态路由请求，userId={}", currentUser.getUserId());

        // 根据用户ID查询路由
        Long userId = Long.valueOf(currentUser.getUserId());
        UserRouteResponse response = routeService.getUserRoutes(userId);

        return Response.success(response);
    }

    /**
     * 检查路由是否存在
     *
     * @param routeName 路由名称
     * @return 是否存在
     */
    @GetMapping("/isRouteExist")
    @Permission
    public Response<Boolean> isRouteExist(@RequestParam String routeName) {
        log.info("收到检查路由是否存在请求，routeName={}", routeName);

        boolean exists = routeService.isRouteExist(routeName);

        return Response.success(exists);
    }
}

