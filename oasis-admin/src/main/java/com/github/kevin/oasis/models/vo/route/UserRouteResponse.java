package com.github.kevin.oasis.models.vo.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户路由响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRouteResponse {

    /**
     * 用户路由列表
     */
    private List<MenuRoute> routes;

    /**
     * 首页路由key
     */
    private String home;
}

