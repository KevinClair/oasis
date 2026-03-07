package com.github.kevin.oasis.services;

import jakarta.servlet.http.HttpServletRequest;

public interface ExecutorAuthService {

    /**
     * 校验执行器请求签名，并返回认证通过的 appCode。
     */
    String verifyAndGetAppCode(HttpServletRequest servletRequest, Object bodyObject);
}
