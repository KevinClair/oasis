package com.github.kevin.oasis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 */
@Data
@ConfigurationProperties(prefix = "oasis.jwt")
public class JwtProperties {

    /** JWT 签名密钥（Base64编码，至少256位） */
    private String secret = "NdS458GLx79HyRdFrR7MXjcrEfFYDG5euKQXAD4z4g4=";

    /** access token 过期时间（毫秒），默认1天 */
    private long expirationMs = 86400000L;

    /** rememberMe token 过期时间（毫秒），默认7天 */
    private long rememberMeExpirationMs = 604800000L;
}
