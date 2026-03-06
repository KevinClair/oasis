package com.github.kevin.oasis.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调度执行日志分片
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobLogChunk {

    private Long id;

    private Long fireLogId;

    private Integer seqNo;

    private Long logTime;

    private String logContent;

    private LocalDateTime createTime;
}
