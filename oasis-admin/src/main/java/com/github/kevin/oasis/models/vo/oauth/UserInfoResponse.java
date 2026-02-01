package com.github.kevin.oasis.models.vo.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户详细信息响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户工号
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

