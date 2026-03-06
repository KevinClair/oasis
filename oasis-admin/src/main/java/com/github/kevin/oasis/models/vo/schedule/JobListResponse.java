package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobListResponse {

    private List<JobVO> records;

    private Integer current;

    private Integer size;

    private Long total;
}
