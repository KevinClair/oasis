package com.github.kevin.oasis.models.vo.application;

import lombok.Data;

/**
 * 应用查询请求类
 */
@Data
public class ApplicationListRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 应用Code
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 当前用户工号（用于数据权限过滤）
     */
    private String currentUserId;
}

