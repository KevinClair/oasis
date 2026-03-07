package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackLogRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackResultRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorHeartbeatRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorRegisterRequest;

public interface ExecutorGatewayService {

    boolean register(ExecutorRegisterRequest request);

    boolean heartbeat(ExecutorHeartbeatRequest request);

    boolean callbackResult(String appCode, ExecutorCallbackResultRequest request);

    boolean callbackLog(String appCode, ExecutorCallbackLogRequest request);
}
