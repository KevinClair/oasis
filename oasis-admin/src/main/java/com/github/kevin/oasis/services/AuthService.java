package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.oauth.ChangePasswordRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginResponse;
import com.github.kevin.oasis.models.vo.oauth.UserInfoResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前登录用户详细信息
     *
     * @param userId 用户工号
     * @return 用户详细信息
     */
    UserInfoResponse getUserInfo(String userId);

    /**
     * 修改密码
     *
     * @param request 修改密码请求参数
     * @return 是否成功
     */
    boolean changePassword(ChangePasswordRequest request);
}
