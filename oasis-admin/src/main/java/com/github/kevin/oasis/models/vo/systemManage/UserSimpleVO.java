package com.github.kevin.oasis.models.vo.systemManage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户简要信息VO（用于下拉选择）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleVO {
    /**
     * 用户ID（主键）
     */
    private Long id;

    /**
     * 工号
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;
}

