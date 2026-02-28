package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联数据访问层
 */
@Mapper
public interface UserRoleDao {

    /**
     * 根据用户ID查询角色ID列表（仅查询启用的角色）
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID查询所有角色ID列表（包括禁用的角色）
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectAllRoleIdsByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<String> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID删除用户角色关联
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID删除用户角色关联
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入用户角色关联
     *
     * @param userRoleList 用户角色关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<UserRole> userRoleList);

    /**
     * 根据用户ID列表删除用户角色关联
     *
     * @param userIds 用户ID列表
     * @return 影响行数
     */
    int deleteByUserIds(@Param("userIds") List<String> userIds);

    /**
     * 根据角色ID列表删除用户角色关联
     *
     * @param roleIds 角色ID列表
     * @return 影响行数
     */
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);
}

