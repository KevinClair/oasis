package com.github.kevin.oasis.starter.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutorRegisterRequest {

    private String appCode;

    private String appKey;

    private String address;

    private String machineTag;

    private String metaJson;
}
