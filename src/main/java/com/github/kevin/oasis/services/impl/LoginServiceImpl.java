package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.global.oauth.UserThreadLocal;
import com.github.kevin.oasis.models.base.UserInfo;
import com.github.kevin.oasis.models.vo.oauth.LoginRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginResponse;
import com.github.kevin.oasis.models.vo.oauth.UserInfoResponse;
import com.github.kevin.oasis.services.LoginService;
import com.github.kevin.oasis.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    @Override
    public LoginResponse login(LoginRequest request) {
        return JwtTokenUtils.generateTokens("111111", request.getUserName(), request.getRememberMe());
    }

    @Override
    public UserInfoResponse getUserInfo() {
        UserInfo userInfo = UserThreadLocal.getUserInfo();
        // todo 根据userInfo去查询详细的用户信息
        return UserInfoResponse.builder().avatar("").userId(userInfo.getUserId()).userName(userInfo.getUserName()).build();
    }
}
