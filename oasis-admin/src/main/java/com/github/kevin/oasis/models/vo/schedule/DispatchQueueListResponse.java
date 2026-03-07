package com.github.kevin.oasis.models.vo.schedule;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * dispatch_queue 分页响应。
 */
@Data
@Builder
public class DispatchQueueListResponse {

    private List<DispatchQueueVO> records;

    private Integer current;

    private Integer size;

    private Long total;
}
