package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.Role;
import com.github.kevin.oasis.models.vo.systemManage.RoleListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问层
 */
@Mapper
public interface RoleDao {

    /**
     * 查询角色列表（分页）
     *
     * @param request 查询参数
     * @return 角色列表
     */
    List<Role> selectRoleList(@Param("request") RoleListRequest request);

    /**
     * 查询角色总数
     *
     * @param request 查询参数
     * @return 角色总数
     */
    Long countRoleList(@Param("request") RoleListRequest request);

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    Role selectById(@Param("id") Long id);

    /**
     * 插入角色
     *
     * @param role 角色信息
     * @return 影响行数
     */
    int insert(Role role);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 影响行数
     */
    int update(Role role);

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     * @return 影响行数
     */
    int deleteRolesByIds(@Param("ids") List<Long> ids);

    /**
     * 切换角色状态（启用/禁用）
     *
     * @param ids 角色ID列表
     * @return 影响行数
     */
    int toggleRoleStatus(@Param("ids") List<Long> ids);

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    Role selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有启用的角色
     *
     * @return 角色列表
     */
    List<Role> selectAllEnabledRoles();
}

