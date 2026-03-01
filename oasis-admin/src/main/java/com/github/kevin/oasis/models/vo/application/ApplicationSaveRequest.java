package com.github.kevin.oasis.models.vo.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 应用保存请求类（新增/编辑）
 */
@Data
public class ApplicationSaveRequest {

    /**
     * 主键ID（编辑时必填）
     */
    private Long id;

    /**
     * 应用Code（全局唯一且必填）
     */
    @NotBlank(message = "应用Code不能为空")
    @Size(max = 100, message = "应用Code长度不能超过100个字符")
    private String appCode;

    /**
     * 应用名称（必填）
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 200, message = "应用名称长度不能超过200个字符")
    private String appName;

    /**
     * 应用描述（必填）
     */
    @NotBlank(message = "应用描述不能为空")
    private String description;

    /**
     * 应用管理员工号列表（非必填，不填默认为创建人）
     */
    private List<String> adminUserIds;

    /**
     * 应用开发者工号列表（非必填）
     */
    private List<String> developerUserIds;

    /**
     * 状态
     */
    private Boolean status;
}

