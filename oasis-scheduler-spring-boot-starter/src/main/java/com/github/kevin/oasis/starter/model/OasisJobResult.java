package com.github.kevin.oasis.starter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Handler execution result.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OasisJobResult {

    private boolean success;

    private boolean retryable;

    private String message;

    public static OasisJobResult success() {
        return OasisJobResult.builder().success(true).retryable(false).build();
    }

    public static OasisJobResult fail(String message) {
        return OasisJobResult.builder().success(false).retryable(false).message(message).build();
    }

    public static OasisJobResult retryable(String message) {
        return OasisJobResult.builder().success(false).retryable(true).message(message).build();
    }
}
