package com.github.kevin.oasis.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.models.entity.Application;
import com.github.kevin.oasis.models.vo.executor.ExecutorInvokeRequest;
import com.github.kevin.oasis.services.DispatchResult;
import com.github.kevin.oasis.services.ExecutorInvokeClient;
import com.github.kevin.oasis.utils.HmacSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Admin -> Executor 下发 HTTP 客户端。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutorInvokeClientImpl implements ExecutorInvokeClient {

    private final SchedulerRuntimeProperties runtimeProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile RestTemplate restTemplate;

    @Override
    public DispatchResult invoke(String address, Application app, Long fireLogId, Integer attemptNo, String handlerName, String triggerParam) {
        try {
            ExecutorInvokeRequest request = ExecutorInvokeRequest.builder()
                    .fireLogId(fireLogId)
                    .attemptNo(attemptNo == null ? 1 : attemptNo)
                    .handlerName(handlerName)
                    .triggerParam(triggerParam)
                    .build();

            String path = normalizeContextPath(runtimeProperties.getExecutorContextPath()) + "/invoke";
            String url = "http://" + address + path;
            String body = objectMapper.writeValueAsString(request);

            // Admin -> Executor 也使用同一套 HMAC 头，形成双向鉴权闭环。
            HttpEntity<String> entity = new HttpEntity<>(body, buildHeaders(app, path, body));
            ResponseEntity<Map> response = getRestTemplate().postForEntity(url, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return DispatchResult.builder().success(false).errorMessage("HTTP状态码=" + response.getStatusCode().value()).build();
            }

            Object responseBody = response.getBody();
            if (responseBody instanceof Map<?, ?> map) {
                if (Boolean.TRUE.equals(map.get("accepted"))) {
                    return DispatchResult.builder().success(true).executorAddress(address).build();
                }
                return DispatchResult.builder().success(false).errorMessage("执行器拒绝接收").build();
            }

            return DispatchResult.builder().success(false).errorMessage("执行器响应格式异常").build();
        } catch (Exception e) {
            log.warn("invoke executor failed, address={}, fireLogId={}, msg={}", address, fireLogId, e.getMessage());
            return DispatchResult.builder().success(false).errorMessage(e.getMessage()).build();
        }
    }

    private HttpHeaders buildHeaders(Application app, String path, String body) {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String signature = HmacSignatureUtil.sign(app.getAppKey(), timestamp, nonce, "POST", path, body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Oasis-App-Code", app.getAppCode());
        headers.add("X-Oasis-Timestamp", String.valueOf(timestamp));
        headers.add("X-Oasis-Nonce", nonce);
        headers.add("X-Oasis-Signature", signature);
        return headers;
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate != null) {
            return restTemplate;
        }
        synchronized (this) {
            if (restTemplate == null) {
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(runtimeProperties.getInvokeConnectTimeoutMs());
                requestFactory.setReadTimeout(runtimeProperties.getInvokeReadTimeoutMs());
                restTemplate = new RestTemplate(requestFactory);
            }
        }
        return restTemplate;
    }

    private String normalizeContextPath(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String path = value.startsWith("/") ? value : "/" + value;
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
