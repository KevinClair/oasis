package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.global.oauth.LoginRateLimiter;
import com.github.kevin.oasis.global.oauth.Permission;
import com.github.kevin.oasis.global.oauth.UserThreadLocal;
import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.base.UserInfo;
import com.github.kevin.oasis.models.vo.oauth.ChangePasswordRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginRequest;
import com.github.kevin.oasis.models.vo.oauth.LoginResponse;
import com.github.kevin.oasis.models.vo.oauth.UserInfoResponse;
import com.github.kevin.oasis.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 登录与认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Response<LoginResponse> login(HttpServletRequest servletRequest,
                                         @Valid @RequestBody LoginRequest request) {
        String clientIp = getClientIp(servletRequest);
        if (!loginRateLimiter.tryAcquire(clientIp)) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "登录过于频繁，请稍后再试");
        }

        log.info("收到登录请求：user={}, rememberMe={}, ip={}", request.getUser(), request.getRememberMe(), clientIp);
        LoginResponse response = authService.login(request);
        return Response.success(response);
    }

    /**
     * 获取当前登录用户详细信息
     */
    @GetMapping("/getUserInfo")
    @Permission
    public Response<UserInfoResponse> getUserInfo() {
        UserInfo currentUser = UserThreadLocal.getUserInfo();
        log.info("收到获取用户信息请求：userId={}", currentUser.getUserId());
        UserInfoResponse response = authService.getUserInfo(currentUser.getUserId());
        return Response.success(response);
    }

    /**
     * 修改密码（需要登录态）
     */
    @PostMapping("/changePassword")
    @Permission
    public Response<Boolean> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UserInfo currentUser = UserThreadLocal.getUserInfo();
        log.info("收到修改密码请求：userAccount={}, operator={}", request.getUserAccount(), currentUser.getUserId());

        // 只能修改自己的密码
        if (!currentUser.getUserId().equals(request.getUserAccount())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "只能修改自己的密码");
        }

        boolean success = authService.changePassword(request);
        return Response.success(success);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
