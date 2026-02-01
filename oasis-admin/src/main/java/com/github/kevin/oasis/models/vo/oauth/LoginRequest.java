package com.github.kevin.oasis.models.vo.oauth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    /**
     * 用户标识（工号或账号）
     */
    @NotBlank(message = "用户标识不能为空")
    private String user;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 记住我
     */
    private Boolean rememberMe;
}

