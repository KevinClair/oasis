package com.github.kevin.oasis.models.vo.systemManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 公告删除请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnouncementDeleteRequest {

    /**
     * 公告ID列表
     */
    @NotEmpty(message = "公告ID列表不能为空")
    private List<Long> ids;
}

