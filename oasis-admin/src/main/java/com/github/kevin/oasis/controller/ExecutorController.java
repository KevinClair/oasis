package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackLogRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackResultRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorHeartbeatRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorRegisterRequest;
import com.github.kevin.oasis.services.ExecutorAuthService;
import com.github.kevin.oasis.services.ExecutorGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 执行器接入网关
 */
@RestController
@RequestMapping("/executor")
@RequiredArgsConstructor
public class ExecutorController {

    private final ExecutorGatewayService executorGatewayService;
    private final ExecutorAuthService executorAuthService;

    @PostMapping("/registry/register")
    public Response<Boolean> register(HttpServletRequest servletRequest, @Valid @RequestBody ExecutorRegisterRequest request) {
        // 网关入口先做签名与时间窗认证，避免伪造请求进入业务逻辑。
        executorAuthService.verifyAndGetAppCode(servletRequest, request);
        return Response.success(executorGatewayService.register(request));
    }

    @PostMapping("/registry/heartbeat")
    public Response<Boolean> heartbeat(HttpServletRequest servletRequest, @Valid @RequestBody ExecutorHeartbeatRequest request) {
        executorAuthService.verifyAndGetAppCode(servletRequest, request);
        return Response.success(executorGatewayService.heartbeat(request));
    }

    @PostMapping("/callback/result")
    public Response<Boolean> callbackResult(HttpServletRequest servletRequest,
                                            @Valid @RequestBody ExecutorCallbackResultRequest request) {
        String appCode = executorAuthService.verifyAndGetAppCode(servletRequest, request);
        return Response.success(executorGatewayService.callbackResult(appCode, request));
    }

    @PostMapping("/callback/log")
    public Response<Boolean> callbackLog(HttpServletRequest servletRequest,
                                         @Valid @RequestBody ExecutorCallbackLogRequest request) {
        String appCode = executorAuthService.verifyAndGetAppCode(servletRequest, request);
        return Response.success(executorGatewayService.callbackLog(appCode, request));
    }
}
