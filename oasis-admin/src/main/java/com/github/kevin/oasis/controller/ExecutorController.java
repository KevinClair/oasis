package com.github.kevin.oasis.controller;

import com.github.kevin.oasis.models.base.Response;
import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackLogRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackResultRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorHeartbeatRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorRegisterRequest;
import com.github.kevin.oasis.services.ExecutorGatewayService;
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

    @PostMapping("/registry/register")
    public Response<Boolean> register(@Valid @RequestBody ExecutorRegisterRequest request) {
        return Response.success(executorGatewayService.register(request));
    }

    @PostMapping("/registry/heartbeat")
    public Response<Boolean> heartbeat(@Valid @RequestBody ExecutorHeartbeatRequest request) {
        return Response.success(executorGatewayService.heartbeat(request));
    }

    @PostMapping("/callback/result")
    public Response<Boolean> callbackResult(@Valid @RequestBody ExecutorCallbackResultRequest request) {
        return Response.success(executorGatewayService.callbackResult(request));
    }

    @PostMapping("/callback/log")
    public Response<Boolean> callbackLog(@Valid @RequestBody ExecutorCallbackLogRequest request) {
        return Response.success(executorGatewayService.callbackLog(request));
    }
}
