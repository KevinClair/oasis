package com.github.kevin.oasis.models.vo.systemManage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色保存请求（新增/编辑）
 */
@Data
public class RoleSaveRequest {

    /**
     * 角色ID（编辑时必传）
     */
    private Long id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色描述
     */
    private String roleDesc;

    /**
     * 状态：true-启用，false-禁用
     */
    private Boolean status;
}

