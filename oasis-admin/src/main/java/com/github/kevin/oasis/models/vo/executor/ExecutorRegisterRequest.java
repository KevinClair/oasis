package com.github.kevin.oasis.models.vo.executor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 执行器注册请求
 */
@Data
public class ExecutorRegisterRequest {

    @NotBlank(message = "应用编码不能为空")
    private String appCode;

    @NotBlank(message = "应用密钥不能为空")
    private String appKey;

    @NotBlank(message = "节点地址不能为空")
    private String address;

    private String machineTag;

    private String metaJson;
}
