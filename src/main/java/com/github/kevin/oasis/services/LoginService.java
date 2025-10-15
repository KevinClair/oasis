package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.oauth.LoginRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginResponse;
import com.github.kevin.oasis.models.vo.oauth.UserInfoResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface LoginService {

    /**
     * 人员登录，登录成功返回 JWT Token 和 Refresh Token
     *
     * @param request 登录请求
     * @return
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取登录人信息
     *
     * @return
     */
    UserInfoResponse getUserInfo();
}
