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

    User selectByUserAccountOrUserId(@Param("user") String user);

    /**
     * @deprecated 密码校验已改用 BCrypt，此方法不再使用。
     */
    @Deprecated
    User selectByUserAccountOrUserIdAndPassword(@Param("user") String user, @Param("password") String password);

    User selectById(@Param("id") Long id);

    int insert(User user);

    int update(User user);

    List<User> selectUserList(@Param("request") UserListRequest request);

    Long countUserList(@Param("request") UserListRequest request);

    int deleteUsersByIds(@Param("ids") List<Long> ids);

    int toggleUserStatus(@Param("ids") List<Long> ids);

    int resetPassword(@Param("ids") List<Long> ids, @Param("password") String password);

    /**
     * 根据用户账号更新密码（新密码已 BCrypt 加密）。
     */
    int updatePassword(@Param("userAccount") String userAccount, @Param("newPassword") String newPassword);

    List<User> selectAllEnabledUsers();
}
