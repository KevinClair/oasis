package com.github.kevin.oasis.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.ApplicationDao;
import com.github.kevin.oasis.models.entity.Application;
import com.github.kevin.oasis.services.ExecutorAuthService;
import com.github.kevin.oasis.utils.HmacSignatureUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行器接口签名认证：
 * 1) 请求头完整性
 * 2) 时间窗校验
 * 3) nonce 防重放
 * 4) HMAC 签名校验
 */
@Service
@RequiredArgsConstructor
public class ExecutorAuthServiceImpl implements ExecutorAuthService {

    private static final String HEADER_APP_CODE = "X-Oasis-App-Code";
    private static final String HEADER_TIMESTAMP = "X-Oasis-Timestamp";
    private static final String HEADER_NONCE = "X-Oasis-Nonce";
    private static final String HEADER_SIGNATURE = "X-Oasis-Signature";

    private final ApplicationDao applicationDao;
    private final SchedulerRuntimeProperties runtimeProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * key: appCode:nonce, value: firstSeenMillis
     */
    private final ConcurrentHashMap<String, Long> nonceCache = new ConcurrentHashMap<>();

    @Override
    public String verifyAndGetAppCode(HttpServletRequest servletRequest, Object bodyObject) {
        if (!runtimeProperties.isExecutorAuthEnabled()) {
            return null;
        }

        String appCode = requiredHeader(servletRequest, HEADER_APP_CODE, "缺少应用标识");
        String timestampText = requiredHeader(servletRequest, HEADER_TIMESTAMP, "缺少时间戳");
        String nonce = requiredHeader(servletRequest, HEADER_NONCE, "缺少随机串");
        String signature = requiredHeader(servletRequest, HEADER_SIGNATURE, "缺少签名");

        long timestamp = parseTimestamp(timestampText);
        validateTimestamp(timestamp);
        validateNonce(appCode, nonce);

        Application app = applicationDao.selectByAppCode(appCode);
        if (app == null || Boolean.FALSE.equals(app.getStatus())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用不存在或已禁用");
        }

        String path = canonicalPath(servletRequest);
        String method = servletRequest.getMethod() == null ? "POST" : servletRequest.getMethod().toUpperCase();
        String body = toJson(bodyObject);

        String expected = HmacSignatureUtil.sign(app.getAppKey(), timestamp, nonce, method, path, body);
        if (!HmacSignatureUtil.constantTimeEquals(expected, signature)) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "签名校验失败");
        }

        return appCode;
    }

    private String requiredHeader(HttpServletRequest request, String headerName, String errMsg) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), errMsg);
        }
        return value.trim();
    }

    private long parseTimestamp(String timestampText) {
        try {
            return Long.parseLong(timestampText);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "时间戳格式非法");
        }
    }

    private void validateTimestamp(long timestamp) {
        long now = System.currentTimeMillis();
        long delta = Math.abs(now - timestamp);
        if (delta > runtimeProperties.getExecutorAuthClockSkewMs()) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "请求时间戳超出允许范围");
        }
    }

    private void validateNonce(String appCode, String nonce) {
        long now = System.currentTimeMillis();
        long ttl = runtimeProperties.getExecutorAuthNonceExpireMs();
        String nonceKey = appCode + ":" + nonce;
        Long exist = nonceCache.putIfAbsent(nonceKey, now);

        if (exist != null) {
            // 已存在且仍在有效窗口内，判定为重放攻击。
            if (now - exist <= ttl) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "重复请求");
            }
            // 已过期 nonce 允许覆盖，避免缓存驻留导致长期误拦截。
            boolean replaced = nonceCache.replace(nonceKey, exist, now);
            if (!replaced) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "重复请求");
            }
        }
        cleanupExpiredNonce();
    }

    private void cleanupExpiredNonce() {
        if (nonceCache.size() > runtimeProperties.getExecutorAuthNonceMaxSize()) {
            long now = System.currentTimeMillis();
            long ttl = runtimeProperties.getExecutorAuthNonceExpireMs();
            Iterator<Map.Entry<String, Long>> it = nonceCache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> entry = it.next();
                if (entry.getValue() == null || now - entry.getValue() > ttl) {
                    it.remove();
                }
            }
        }
    }

    private String canonicalPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private String toJson(Object bodyObject) {
        try {
            return objectMapper.writeValueAsString(bodyObject);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "请求体序列化失败");
        }
    }
}
