package com.github.kevin.oasis.models.vo.systemManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO {

    /**
     * 主键ID
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
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别：1-男，2-女
     */
    private String userGender;

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

    /**
     * 用户角色编码集合
     */
    private List<String> userRoles;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

