package com.github.kevin.oasis.models.vo.systemManage;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 菜单删除请求
 */
@Data
public class MenuDeleteRequest {

    /**
     * 菜单ID列表
     */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> ids;
}

