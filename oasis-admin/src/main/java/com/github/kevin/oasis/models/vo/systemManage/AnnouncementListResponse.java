package com.github.kevin.oasis.models.vo.systemManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 公告列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnouncementListResponse {

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

    /**
     * 公告列表
     */
    private List<AnnouncementVO> records;
}

