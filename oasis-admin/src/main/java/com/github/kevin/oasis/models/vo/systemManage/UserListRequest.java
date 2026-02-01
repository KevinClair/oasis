package com.github.kevin.oasis.models.vo.systemManage;

import lombok.Data;

/**
 * 用户列表查询请求参数
 */
@Data
public class UserListRequest {

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

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
     * 性别：1-男，2-女
     */
    private String userGender;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 状态：true-启用，false-禁用
     */
    private Boolean status;
}

