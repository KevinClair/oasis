package com.github.kevin.oasis.models.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {

    /**
     * 用户工号（存储在JWT中）
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 角色信息
     */
    private List<String> roles;

    /**
     * 权限信息
     */
    private List<String> permissions;
}
