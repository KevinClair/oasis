package com.github.kevin.oasis.models.vo.oauth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    private String userAccount;

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码（必须包含字母和数字，至少6位）
     */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$",
             message = "新密码必须包含字母和数字，且长度至少为6位")
    private String newPassword;
}

