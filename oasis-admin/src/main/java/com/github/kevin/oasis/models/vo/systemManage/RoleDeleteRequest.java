package com.github.kevin.oasis.models.vo.systemManage;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 角色删除请求
 */
@Data
public class RoleDeleteRequest {

    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> ids;
}

