package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.UserDao;
import com.github.kevin.oasis.models.entity.User;
import com.github.kevin.oasis.models.vo.systemManage.*;
import com.github.kevin.oasis.services.UserManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManageServiceImpl implements UserManageService {

    private final UserDao userDao;

    @Override
    public UserListResponse getUserList(UserListRequest request) {
        log.info("查询用户列表，参数：{}", request);

        // 计算分页参数
        int offset = 0;
        if (request.getCurrent() != null && request.getSize() != null) {
            offset = (request.getCurrent() - 1) * request.getSize();
        }
        request.setCurrent(offset);

        // 查询用户列表
        List<User> userList = userDao.selectUserList(request);

        // 查询总数
        Long total = userDao.countUserList(request);

        // 转换为VO
        List<UserVO> userVOList = userList.stream().map(user ->
            UserVO.builder()
                    .id(user.getId())
                    .userId(user.getUserId())
                    .userAccount(user.getUserAccount())
                    .userName(user.getUserName())
                    .nickName(user.getNickName())
                    .userGender(user.getUserGender())
                    .userPhone(user.getPhone())
                    .userEmail(user.getEmail())
                    .status(user.getStatus())
                    .userRoles(user.getUserRoles() != null ? user.getUserRoles() : new ArrayList<>())
                    .createBy(user.getCreateBy())
                    .createTime(user.getCreateTime())
                    .updateBy(user.getUpdateBy())
                    .updateTime(user.getUpdateTime())
                    .build()
        ).collect(Collectors.toList());

        // 构建响应
        UserListResponse response = UserListResponse.builder()
                .current(request.getCurrent() / request.getSize() + 1)
                .size(request.getSize())
                .total(total)
                .records(userVOList)
                .build();

        log.info("查询用户列表成功，总数：{}，当前页：{}，每页大小：{}", total, response.getCurrent(), response.getSize());

        return response;
    }

    @Override
    public int deleteUsers(UserDeleteRequest request) {
        log.info("删除用户，参数：{}", request);

        if (request.getIds() == null || request.getIds().isEmpty()) {
            log.warn("删除用户失败：用户ID列表为空");
            return 0;
        }

        // 批量删除用户
        int deletedCount = userDao.deleteUsersByIds(request.getIds());

        log.info("删除用户成功，删除数量：{}", deletedCount);

        return deletedCount;
    }

    @Override
    public int toggleUserStatus(UserToggleStatusRequest request) {
        log.info("切换用户状态，参数：{}", request);

        if (request.getIds() == null || request.getIds().isEmpty()) {
            log.warn("切换用户状态失败：用户ID列表为空");
            return 0;
        }

        // 批量切换用户状态
        int updatedCount = userDao.toggleUserStatus(request.getIds());

        log.info("切换用户状态成功，更新数量：{}", updatedCount);

        return updatedCount;
    }

    @Override
    public Long saveUser(UserSaveRequest request) {
        log.info("保存用户，参数：{}", request);

        if (request.getId() == null) {
            // 新增用户
            if (!StringUtils.hasText(request.getPassword())) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "新增用户时密码不能为空");
            }

            // 检查用户账号是否已存在
            if (StringUtils.hasText(request.getUserAccount())) {
                User existingUser = userDao.selectByUserAccountOrUserId(request.getUserAccount());
                if (existingUser != null) {
                    throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户账号已存在");
                }
            }

            // 检查工号是否已存在
            if (request.getUserId() != null) {
                User existingUser = userDao.selectByUserAccountOrUserId(String.valueOf(request.getUserId()));
                if (existingUser != null) {
                    throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户工号已存在");
                }
            }

            User user = User.builder()
                    .userId(request.getUserId())
                    .userAccount(request.getUserAccount())
                    .userName(request.getUserName())
                    .password(request.getPassword()) // TODO: 需要加密
                    .nickName(request.getNickName())
                    .userGender(request.getUserGender())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .status(request.getStatus() != null ? request.getStatus() : true)
                    .userRoles(request.getUserRoles())
                    .createBy("admin") // TODO: 从当前登录用户获取
                    .updateBy("admin")
                    .build();

            userDao.insert(user);
            log.info("新增用户成功，用户ID：{}", user.getId());
            return user.getId();
        } else {
            // 编辑用户
            User user = userDao.selectById(request.getId());
            if (user == null) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户不存在");
            }

            // 检查用户账号是否被其他用户使用
            if (StringUtils.hasText(request.getUserAccount())) {
                User existingUser = userDao.selectByUserAccountOrUserId(request.getUserAccount());
                if (existingUser != null && !existingUser.getId().equals(request.getId())) {
                    throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户账号已存在");
                }
            }

            // 注意：编辑用户时不允许修改工号（userId）
            // user.setUserId(request.getUserId()); // 工号不可修改
            user.setUserAccount(request.getUserAccount());
            user.setUserName(request.getUserName());
            if (StringUtils.hasText(request.getPassword())) {
                user.setPassword(request.getPassword()); // TODO: 需要加密
            }
            user.setNickName(request.getNickName());
            user.setUserGender(request.getUserGender());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setStatus(request.getStatus());
            user.setUserRoles(request.getUserRoles());
            user.setUpdateBy("admin"); // TODO: 从当前登录用户获取

            userDao.update(user);
            log.info("更新用户成功，用户ID：{}", user.getId());
            return user.getId();
        }
    }

    @Override
    public UserVO getUserById(Long id) {
        log.info("查询用户详情，用户ID：{}", id);

        User user = userDao.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户不存在");
        }

        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .userGender(user.getUserGender())
                .userPhone(user.getPhone())
                .userEmail(user.getEmail())
                .status(user.getStatus())
                .userRoles(user.getUserRoles() != null ? user.getUserRoles() : new ArrayList<>())
                .createBy(user.getCreateBy())
                .createTime(user.getCreateTime())
                .updateBy(user.getUpdateBy())
                .updateTime(user.getUpdateTime())
                .build();

        log.info("查询用户详情成功");

        return userVO;
    }
}

