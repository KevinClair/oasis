package com.github.kevin.oasis.models.vo.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单路由响应（符合前端ElegantConstRoute格式）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuRoute {

    /**
     * 路由ID（菜单ID）
     */
    private String id;

    /**
     * 路由名称（必须唯一）
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 路由组件（使用点号分隔的字符串格式）
     */
    private String component;

    /**
     * 路由元信息
     */
    private RouteMeta meta;

    /**
     * 子路由列表
     */
    private List<MenuRoute> children;
}

