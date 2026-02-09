package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 菜单实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Menu {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 父级菜单ID
     */
    private Long parentId;

    /**
     * 菜单类型：1-目录，2-菜单
     */
    private Integer menuType;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 路由名称
     */
    private String routeName;

    /**
     * 路由路径
     */
    private String routePath;

    /**
     * 路径参数
     */
    private String pathParam;

    /**
     * 组件名称
     */
    private String component;

    /**
     * 图标类型：1-iconify图标，2-本地图标
     */
    private String iconType;

    /**
     * 图标
     */
    private String icon;

    /**
     * 本地图标
     */
    private String localIcon;

    /**
     * 国际化key
     */
    private String i18nKey;

    /**
     * 排序
     */
    private Integer order;

    /**
     * 是否缓存：true-缓存，false-不缓存
     */
    private Boolean keepAlive;

    /**
     * 是否常量路由：true-是，false-否
     */
    private Boolean constant;

    /**
     * 外链：外部链接地址
     */
    private String href;

    /**
     * 是否隐藏菜单：true-隐藏，false-显示
     */
    private Boolean hideInMenu;

    /**
     * 激活的菜单key
     */
    private String activeMenu;

    /**
     * 支持多个tab页签：true-支持，false-不支持
     */
    private Boolean multiTab;

    /**
     * 固定在tab卡上的索引位置
     */
    private Integer fixedIndexInTab;

    /**
     * 状态：true-启用，false-禁用
     */
    private Boolean status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

