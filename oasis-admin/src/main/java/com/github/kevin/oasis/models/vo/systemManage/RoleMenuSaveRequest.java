package com.github.kevin.oasis.models.vo.systemManage;

import lombok.Data;

import java.util.List;

/**
 * 角色菜单权限保存请求
 */
@Data
public class RoleMenuSaveRequest {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID列表
     */
    private List<Long> menuIds;
}

