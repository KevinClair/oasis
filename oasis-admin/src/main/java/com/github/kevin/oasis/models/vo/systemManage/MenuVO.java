package com.github.kevin.oasis.models.vo.systemManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单信息VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuVO {

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
     * 固定在tab卡上的索引位置
     */
    private Integer fixedIndexInTab;

    /**
     * 状态
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

    /**
     * 子菜单列表
     */
    private List<MenuVO> children;
}

