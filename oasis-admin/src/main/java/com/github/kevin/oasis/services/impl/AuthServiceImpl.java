package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.UserDao;
import com.github.kevin.oasis.models.entity.User;
import com.github.kevin.oasis.models.vo.oauth.LoginRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginResponse;
import com.github.kevin.oasis.models.vo.oauth.UserInfoResponse;
import com.github.kevin.oasis.services.AuthService;
import com.github.kevin.oasis.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 参数校验已通过@Valid注解在Controller层完成

        // 根据用户账号或工号和密码查询用户信息
        User user = userDao.selectByUserAccountOrUserIdAndPassword(request.getUser(), request.getPassword());

        // 如果查不到用户信息，返回NEED_LOGIN异常
        if (user == null) {
            log.warn("登录失败：用户标识或密码错误, user={}", request.getUser());
            throw new BusinessException(ResponseStatusEnums.NEED_LOGIN.getCode(), "用户标识或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != null && !user.getStatus()) {
            log.warn("登录失败：用户已被禁用, user={}, userId={}", request.getUser(), user.getUserId());
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户已被禁用");
        }

        // 使用JwtTokenUtils生成token和refreshToken
        // rememberMe为true时，token有效期为7天，否则为1天
        String token = JwtTokenUtils.generateTokens(
                String.valueOf(user.getId()),
                user.getUserName(),
                request.getRememberMe()
        );

        // 构建完整的响应对象
        LoginResponse response = LoginResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .token(token)
                .refreshToken(UUID.randomUUID().toString())
                .build();

        log.info("用户登录成功：userId={}, userAccount={}, userName={}, rememberMe={}, tokenExpiration={}",
                user.getUserId(), user.getUserAccount(), user.getUserName(), request.getRememberMe(),
                request.getRememberMe() != null && request.getRememberMe() ? "7天" : "1天");

        return response;
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        // 根据用户ID查询用户信息
        User user = userDao.selectById(userId);

        // 如果查不到用户信息，抛出异常
        if (user == null) {
            log.warn("查询用户信息失败：用户不存在, userId={}", userId);
            throw new BusinessException(ResponseStatusEnums.PARAM_ERROR.getCode(), "用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() != null && !user.getStatus()) {
            log.warn("查询用户信息失败：用户已被禁用, userId={}", userId);
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户已被禁用");
        }

        // 构建用户详细信息响应
        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus() != null && user.getStatus() ? 1 : 0)
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();

        log.info("查询用户信息成功：id={}, userId={}, userName={}", user.getId(), user.getUserId(), user.getUserName());

        return response;
    }
}

