package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.oauth.LoginResponse;
import com.github.kevin.oasis.models.vo.oauth.LoginRequest;
import com.github.kevin.oasis.models.vo.oauth.UserInfoResponse;
import com.github.kevin.oasis.services.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final LoginService loginService;

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Response<LoginResponse> login(@RequestBody LoginRequest request) {
        return Response.success(loginService.login(request));
    }

    /**
     * 获取登录用户信息
     *
     * @return
     */
    @GetMapping("/getUserInfo")
    @Permission
    public Response<UserInfoResponse> getUserInfo(){
        return Response.success(loginService.getUserInfo());
    }
}
