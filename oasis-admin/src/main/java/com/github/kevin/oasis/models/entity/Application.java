package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 应用管理实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 应用Code（全局唯一）
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用密钥（base64编码）
     */
    private String appKey;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 应用管理员工号列表（JSON数组）
     */
    private List<String> adminUserIds;

    /**
     * 应用开发者工号列表（JSON数组）
     */
    private List<String> developerUserIds;

    /**
     * 状态：true-启用，false-禁用
     */
    private Boolean status;

    /**
     * 创建人工号
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人工号
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;
}

