package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 调度日志列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobLogListResponse {

    private List<JobLogVO> records;

    private Integer current;

    private Integer size;

    private Long total;
}
