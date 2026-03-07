package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分片租约记录。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShardLease {

    private Long id;

    private Integer shardId;

    private String ownerNodeId;

    private Long leaseExpireAt;

    private Long version;

    private LocalDateTime updateTime;
}
