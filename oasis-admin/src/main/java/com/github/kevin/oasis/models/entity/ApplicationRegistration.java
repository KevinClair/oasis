package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 应用注册信息实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationRegistration {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 应用Code
     */
    private String appCode;

    /**
     * 注册IP地址
     */
    private String ipAddress;

    /**
     * 注册机器标签
     */
    private String machineTag;

    /**
     * 注册时间
     */
    private Date registerTime;

    /**
     * 额外信息（JSON格式）
     */
    private String extraInfo;
}

