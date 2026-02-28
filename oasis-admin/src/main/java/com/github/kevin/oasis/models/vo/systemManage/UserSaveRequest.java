package com.github.kevin.oasis.models.vo.systemManage;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用户保存请求（新增/编辑）
 */
@Data
public class UserSaveRequest {

    /**
     * 用户ID（编辑时必传）
     */
    private Long id;

    /**
     * 用户工号
     */
    private String userId;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String userName;

    /**
     * 密码（新增时必填）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别：1-男，2-女
     */
    private String userGender;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态：true-启用，false-禁用
     */
    private Boolean status;

    /**
     * 用户角色列表（角色编码数组）
     */
    private List<String> userRoles;
}

