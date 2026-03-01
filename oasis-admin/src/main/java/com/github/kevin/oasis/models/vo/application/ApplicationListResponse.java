package com.github.kevin.oasis.models.vo.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 应用列表响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationListResponse {

    /**
     * 记录列表
     */
    private List<ApplicationVO> records;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总记录数
     */
    private Long total;
}

