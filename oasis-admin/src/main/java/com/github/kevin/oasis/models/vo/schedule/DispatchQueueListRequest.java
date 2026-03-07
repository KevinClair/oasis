package com.github.kevin.oasis.models.vo.schedule;

import lombok.Data;

/**
 * dispatch_queue 分页查询请求。
 */
@Data
public class DispatchQueueListRequest {

    private Integer current = 1;

    private Integer size = 10;

    private String status;

    private Long fireLogId;

    private Long jobId;
}
