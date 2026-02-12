package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联数据访问层
 */
@Mapper
public interface RoleMenuDao {

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID删除角色菜单关联
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色菜单关联
     *
     * @param roleMenuList 角色菜单关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<RoleMenu> roleMenuList);

    /**
     * 根据角色ID列表删除角色菜单关联
     *
     * @param roleIds 角色ID列表
     * @return 影响行数
     */
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据菜单ID删除角色菜单关联
     *
     * @param menuId 菜单ID
     * @return 影响行数
     */
    int deleteByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据菜单ID列表删除角色菜单关联
     *
     * @param menuIds 菜单ID列表
     * @return 影响行数
     */
    int deleteByMenuIds(@Param("menuIds") List<Long> menuIds);
}

