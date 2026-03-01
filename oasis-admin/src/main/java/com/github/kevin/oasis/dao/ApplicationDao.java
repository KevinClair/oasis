package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.Application;
import com.github.kevin.oasis.models.vo.application.ApplicationListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应用管理DAO
 */
@Mapper
public interface ApplicationDao {

    /**
     * 查询应用列表（带数据权限过滤）
     *
     * @param request 查询参数
     * @return 应用列表
     */
    List<Application> selectApplicationList(ApplicationListRequest request);

    /**
     * 查询应用总数（带数据权限过滤）
     *
     * @param request 查询参数
     * @return 总数
     */
    Long countApplicationList(ApplicationListRequest request);

    /**
     * 根据ID查询应用
     *
     * @param id 应用ID
     * @return 应用信息
     */
    Application selectById(@Param("id") Long id);

    /**
     * 根据应用Code查询应用
     *
     * @param appCode 应用Code
     * @return 应用信息
     */
    Application selectByAppCode(@Param("appCode") String appCode);

    /**
     * 新增应用
     *
     * @param application 应用信息
     * @return 影响行数
     */
    int insert(Application application);

    /**
     * 更新应用
     *
     * @param application 应用信息
     * @return 影响行数
     */
    int update(Application application);

    /**
     * 批量删除应用
     *
     * @param ids 应用ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 检查用户是否有权限修改/删除应用（必须是管理员）
     *
     * @param id 应用ID
     * @param userId 用户工号
     * @return true-有权限，false-无权限
     */
    boolean checkPermission(@Param("id") Long id, @Param("userId") String userId);
}

