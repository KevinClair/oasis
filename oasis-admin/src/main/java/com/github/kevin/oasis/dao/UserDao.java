package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.User;
import com.github.kevin.oasis.models.vo.systemManage.UserListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserDao {

    /**
     * 根据用户账号或工号查询用户
     *
     * @param user 用户账号或工号
     * @return 用户信息
     */
    User selectByUserAccountOrUserId(@Param("user") String user);

    /**
     * 根据用户账号或工号和密码查询用户
     *
     * @param user 用户账号或工号
     * @param password 密码
     * @return 用户信息
     */
    User selectByUserAccountOrUserIdAndPassword(@Param("user") String user, @Param("password") String password);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名和密码查询用户 (已废弃，请使用selectByUserAccountOrUserIdAndPassword)
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @Deprecated
    User selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    /**
     * 插入用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 查询用户列表（分页）
     *
     * @param request 查询参数
     * @return 用户列表
     */
    List<User> selectUserList(@Param("request") UserListRequest request);

    /**
     * 查询用户总数
     *
     * @param request 查询参数
     * @return 用户总数
     */
    Long countUserList(@Param("request") UserListRequest request);

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 影响行数
     */
    int deleteUsersByIds(@Param("ids") List<Long> ids);

    /**
     * 切换用户状态（启用/禁用）
     *
     * @param ids 用户ID列表
     * @return 影响行数
     */
    int toggleUserStatus(@Param("ids") List<Long> ids);
}

