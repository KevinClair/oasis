package com.github.kevin.oasis.models.vo.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 告警事件列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAlarmEventListResponse {

    private List<JobAlarmEventVO> records;

    private Integer current;

    private Integer size;

    private Long total;
}
