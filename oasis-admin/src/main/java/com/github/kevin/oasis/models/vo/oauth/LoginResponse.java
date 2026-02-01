package com.github.kevin.oasis.models.vo.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

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
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;
}

