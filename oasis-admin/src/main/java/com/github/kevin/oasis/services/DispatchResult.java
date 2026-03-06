package com.github.kevin.oasis.services;

import lombok.Builder;
import lombok.Data;

/**
 * 执行器下发结果
 */
@Data
@Builder
public class DispatchResult {

    private boolean success;

    private String executorAddress;

    private String errorMessage;
}
