package com.github.kevin.oasis.models.vo.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 应用管理VO类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 应用Code
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用密钥
     */
    private String appKey;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 应用管理员工号列表
     */
    private List<String> adminUserIds;

    /**
     * 应用管理员姓名列表
     */
    private List<String> adminUserNames;

    /**
     * 应用管理员账号列表
     */
    private List<String> adminUserAccounts;

    /**
     * 应用开发者工号列表
     */
    private List<String> developerUserIds;

    /**
     * 应用开发者姓名列表
     */
    private List<String> developerNames;

    /**
     * 应用开发者账号列表
     */
    private List<String> developerAccounts;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建人工号
     */
    private String createBy;

    /**
     * 创建人姓名
     */
    private String createByName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人工号
     */
    private String updateBy;

    /**
     * 更新人姓名
     */
    private String updateByName;

    /**
     * 更新时间
     */
    private Date updateTime;
}

