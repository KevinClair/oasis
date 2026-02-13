package com.github.kevin.oasis.models.vo.systemManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公告查询请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnouncementListRequest {

    /**
     * 当前页码（偏移量）
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 公告标题（模糊查询）
     */
    private String title;

    /**
     * 公告类型
     */
    private String type;
}

