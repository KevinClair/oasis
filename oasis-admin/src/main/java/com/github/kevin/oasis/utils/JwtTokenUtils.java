package com.github.kevin.oasis.utils;

import com.github.kevin.oasis.config.JwtProperties;
import com.github.kevin.oasis.models.base.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Token 工具（Spring Bean，密钥由 application.properties 的 oasis.jwt.secret 注入）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenUtils {

    private static final String JWT_CLAIM_USER_ID = "userId";
    private static final String JWT_CLAIM_USER_NAME = "userName";

    private final JwtProperties jwtProperties;

    private SecretKeySpec signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        if (keyBytes.length < 32) {
            log.warn("JWT secret key is shorter than 256 bits, consider generating a stronger key");
        }
        this.signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * 根据 userId 和 userName 生成 token。
     */
    public String generateToken(String userId, String userName, Boolean rememberMe) {
        long expiration = Boolean.TRUE.equals(rememberMe)
                ? jwtProperties.getRememberMeExpirationMs()
                : jwtProperties.getExpirationMs();

        return Jwts.builder()
                .claim(JWT_CLAIM_USER_ID, userId)
                .claim(JWT_CLAIM_USER_NAME, userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 根据 token 解析出 UserInfo，解析失败返回 null。
     */
    public UserInfo parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UserInfo.builder()
                    .userId(claims.get(JWT_CLAIM_USER_ID, String.class))
                    .userName(claims.get(JWT_CLAIM_USER_NAME, String.class))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
