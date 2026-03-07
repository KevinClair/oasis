package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Admin 调度节点信息。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchedulerNode {

    private Long id;

    private String nodeId;

    private String host;

    private Integer port;

    private String status;

    private Long lastHeartbeatTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
