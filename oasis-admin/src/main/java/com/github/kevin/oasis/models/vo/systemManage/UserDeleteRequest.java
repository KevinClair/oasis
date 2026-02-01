package com.github.kevin.oasis.models.vo.systemManage;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 用户删除请求
 */
@Data
public class UserDeleteRequest {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> ids;
}

