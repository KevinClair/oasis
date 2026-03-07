package com.github.kevin.oasis.starter.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.starter.autoconfigure.OasisSchedulerProperties;
import com.github.kevin.oasis.starter.model.ExecutorCallbackLogRequest;
import com.github.kevin.oasis.starter.model.ExecutorCallbackResultRequest;
import com.github.kevin.oasis.starter.model.ExecutorHeartbeatRequest;
import com.github.kevin.oasis.starter.model.ExecutorRegisterRequest;
import com.github.kevin.oasis.starter.util.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP client for Oasis admin callbacks and registry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OasisAdminClient {

    private final RestTemplate restTemplate;
    private final OasisSchedulerProperties properties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean register(ExecutorRegisterRequest request) {
        return post("/executor/registry/register", request);
    }

    public boolean heartbeat(ExecutorHeartbeatRequest request) {
        return post("/executor/registry/heartbeat", request);
    }

    public boolean callbackResult(ExecutorCallbackResultRequest request) {
        return post("/executor/callback/result", request);
    }

    public boolean callbackLog(ExecutorCallbackLogRequest request) {
        return post("/executor/callback/log", request);
    }

    private boolean post(String path, Object body) {
        String url = properties.getAdmin().getBaseUrl() + path;
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            // 所有请求都使用 appKey 对 method/path/bodyHash 进行 HMAC 签名。
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, buildHeaders(path, jsonBody));
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("request oasis admin failed, path={}, status={}", path, response.getStatusCode().value());
                return false;
            }

            Map<?, ?> data = response.getBody();
            if (data == null) {
                log.warn("request oasis admin empty body, path={}", path);
                return false;
            }

            Object code = data.get("code");
            boolean success = "0000".equals(String.valueOf(code));
            if (!success) {
                log.warn("request oasis admin business failed, path={}, code={}, msg={}", path, code, data.get("msg"));
            }
            return success;
        } catch (JsonProcessingException e) {
            log.error("serialize request body failed, path={}", path, e);
            return false;
        } catch (Exception e) {
            log.warn("request oasis admin failed, path={}, msg={}", path, e.getMessage());
            return false;
        }
    }

    private HttpHeaders buildHeaders(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        long timestamp = System.currentTimeMillis();
        String nonce = SignatureUtil.nonce();
        String signature = SignatureUtil.sign(properties.getAppKey(), timestamp, nonce, "POST", path, body);

        headers.add("X-Oasis-App-Code", properties.getAppCode());
        headers.add("X-Oasis-Timestamp", String.valueOf(timestamp));
        headers.add("X-Oasis-Nonce", nonce);
        headers.add("X-Oasis-Signature", signature);
        return headers;
    }
}
