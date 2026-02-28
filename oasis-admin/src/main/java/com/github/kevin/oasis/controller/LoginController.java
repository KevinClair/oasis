package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.global.oauth.UserThreadLocal;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.base.UserInfo;
import com.github.kevin.oasis.models.vo.oauth.ChangePasswordRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginResponse;
import com.github.kevin.oasis.models.vo.oauth.UserInfoResponse;
import com.github.kevin.oasis.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;

    /**
     * 用户登录接口
     *
     * @param request 登录请求参数
     * @return 登录响应
     */
    @PostMapping("/login")
    public Response<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求：user={}, rememberMe={}", request.getUser(), request.getRememberMe());
        LoginResponse response = authService.login(request);
        return Response.success(response);
    }

    /**
     * 获取当前登录用户详细信息
     *
     * @return 用户详细信息
     */
    @GetMapping("/getUserInfo")
    @Permission
    public Response<UserInfoResponse> getUserInfo() {
        // 从ThreadLocal中获取当前登录用户信息
        UserInfo currentUser = UserThreadLocal.getUserInfo();
        log.info("收到获取用户信息请求：userId={}", currentUser.getUserId());

        // 根据用户工号查询详细信息
        UserInfoResponse response = authService.getUserInfo(currentUser.getUserId());

        return Response.success(response);
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求参数
     * @return 成功响应
     */
    @PostMapping("/changePassword")
    public Response<Boolean> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("收到修改密码请求：userAccount={}", request.getUserAccount());

        boolean success = authService.changePassword(request);

        return Response.success(success);
    }
}

