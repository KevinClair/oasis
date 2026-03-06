package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户端执行节点
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutorNode {

    private Long id;

    private String appCode;

    private String address;

    private String machineTag;

    private String status;

    private Long lastHeartbeatTime;

    private String metaJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
