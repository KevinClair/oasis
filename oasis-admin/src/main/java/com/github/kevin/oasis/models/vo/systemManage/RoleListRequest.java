package com.github.kevin.oasis.models.vo.systemManage;

import lombok.Data;

/**
 * 角色列表查询请求参数
 */
@Data
public class RoleListRequest {

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 状态：true-启用，false-禁用
     */
    private Boolean status;
}

