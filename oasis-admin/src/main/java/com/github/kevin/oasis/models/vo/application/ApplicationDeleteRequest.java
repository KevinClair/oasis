package com.github.kevin.oasis.models.vo.application;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 应用删除请求类
 */
@Data
public class ApplicationDeleteRequest {

    /**
     * 应用ID列表
     */
    @NotEmpty(message = "应用ID列表不能为空")
    private List<Long> ids;
}

