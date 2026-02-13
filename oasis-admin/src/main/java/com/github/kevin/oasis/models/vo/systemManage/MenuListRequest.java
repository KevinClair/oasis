package com.github.kevin.oasis.models.vo.systemManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单列表查询请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuListRequest {

    /**
     * 常量数据筛选：null-全部，true-仅常量路由，false-仅动态路由
     */
    private Boolean constant;

    /**
     * 状态筛选：null-全部，true-仅启用，false-仅禁用
     */
    private Boolean status;
}

