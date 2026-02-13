package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.Menu;
import com.github.kevin.oasis.models.vo.systemManage.MenuListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单数据访问层
 */
@Mapper
public interface MenuDao {

    /**
     * 查询菜单列表（支持常量和状态筛选）
     *
     * @param request 查询参数
     * @return 菜单列表
     */
    List<Menu> selectMenuList(@Param("request") MenuListRequest request);

    /**
     * 查询所有常量菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> selectConstantMenus();

    /**
     * 查询所有非常量菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> selectNotConstantMenus();

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    Menu selectById(@Param("id") Long id);

    /**
     * 根据路由名称查询菜单
     *
     * @param routeName 路由名称
     * @return 菜单信息
     */
    Menu selectByRouteName(@Param("routeName") String routeName);

    /**
     * 插入菜单
     *
     * @param menu 菜单信息
     * @return 影响行数
     */
    int insert(Menu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单信息
     * @return 影响行数
     */
    int update(Menu menu);

    /**
     * 批量删除菜单
     *
     * @param ids 菜单ID列表
     * @return 影响行数
     */
    int deleteMenusByIds(@Param("ids") List<Long> ids);

    /**
     * 根据父级菜单ID查询子菜单
     *
     * @param parentId 父级菜单ID
     * @return 子菜单列表
     */
    List<Menu> selectByParentId(@Param("parentId") Long parentId);
}

