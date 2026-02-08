package com.github.kevin.oasis.models.vo.systemManage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单保存请求（新增/编辑）
 */
@Data
public class MenuSaveRequest {

    /**
     * 菜单ID（编辑时必传）
     */
    private Long id;

    /**
     * 父级菜单ID
     */
    private Long parentId;

    /**
     * 菜单类型：1-目录，2-菜单
     */
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 路由名称
     */
    @NotBlank(message = "路由名称不能为空")
    private String routeName;

    /**
     * 路由路径
     */
    @NotBlank(message = "路由路径不能为空")
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
     * 是否缓存
     */
    private Boolean keepAlive;

    /**
     * 是否常量路由
     */
    private Boolean constant;

    /**
     * 外链
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
     * 固定在tab卡上
     */
    private Boolean fixedIndexInTab;

    /**
     * 状态
     */
    private Boolean status;
}

