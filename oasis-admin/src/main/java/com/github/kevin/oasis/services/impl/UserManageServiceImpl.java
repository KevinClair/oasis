package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.dao.UserDao;
import com.github.kevin.oasis.models.entity.User;
import com.github.kevin.oasis.models.vo.systemManage.UserDeleteRequest;
import com.github.kevin.oasis.models.vo.systemManage.UserListRequest;
import com.github.kevin.oasis.models.vo.systemManage.UserListResponse;
import com.github.kevin.oasis.models.vo.systemManage.UserToggleStatusRequest;
import com.github.kevin.oasis.models.vo.systemManage.UserVO;
import com.github.kevin.oasis.services.UserManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                    .userRoles(new ArrayList<>()) // TODO: 从用户角色关联表查询
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
}

