package com.github.kevin.oasis.models.vo.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 路由元信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteMeta {

    /**
     * 路由标题
     */
    private String title;

    /**
     * 国际化key
     */
    private String i18nKey;

    /**
     * 路由排序
     */
    private Integer order;

    /**
     * 图标
     */
    private String icon;

    /**
     * 本地图标
     */
    private String localIcon;

    /**
     * 是否缓存
     */
    private Boolean keepAlive;

    /**
     * 是否常量路由
     */
    private Boolean constant;

    /**
     * 外部链接
     */
    private String href;

    /**
     * 是否隐藏菜单
     */
    private Boolean hideInMenu;

    /**
     * 激活的菜单key
     */
    private String activeMenu;

    /**
     * 支持多个tab页签
     */
    private Boolean multiTab;

    /**
     * 固定在tab卡上的索引
     */
    private Integer fixedIndexInTab;

    /**
     * 路由角色权限
     */
    private List<String> roles;
}

