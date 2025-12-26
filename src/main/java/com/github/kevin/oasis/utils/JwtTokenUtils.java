package com.github.kevin.oasis.utils;

import com.github.kevin.oasis.models.base.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * JWT Token 工具类
 */
public class JwtTokenUtils {
    private static final String SECRET_KEY = "NdS458GLx79HyRdFrR7MXjcrEfFYDG5euKQXAD4z4g4=";

    private static final long JWT_TOKEN_EXPIRATION_TIME = 86400000; // 1 天 (24小时 * 60分钟 * 60秒 * 1000毫秒)
    private static final long JWT_TOKEN_EXPIRATION_TIME_WEEK = 86400000 * 7; // 7 天

    private static final String JWT_SUBJECT = "user";
    private static final String JWT_CLAIM_USER_ID = "userId";
    private static final String JWT_CLAIM_USER_NAME = "userName";

    // 根据 userId 和 userName 生成 token 和 refreshToken
    public static String generateTokens(String userId, String userName, Boolean rememberMe) {
        // 生成 Token
        return Jwts.builder()
                .setSubject(JWT_SUBJECT)
                .claim(JWT_CLAIM_USER_ID, userId)
                .claim(JWT_CLAIM_USER_NAME, userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (rememberMe? JWT_TOKEN_EXPIRATION_TIME_WEEK: JWT_TOKEN_EXPIRATION_TIME)))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 根据 token 解析出 userId 和 userName
    public static UserInfo parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY).build()
                    .parseClaimsJws(token)
                    .getBody();
            return UserInfo.builder().userId(claims.get(JWT_CLAIM_USER_ID, String.class)).userName(claims.get(JWT_CLAIM_USER_NAME, String.class)).build();
        } catch (Exception e) {
            return null; // token 无效
        }
    }

    // 根据 refreshToken 刷新 token
    public static String refreshToken(String refreshToken, UserInfo userInfo, long expirationTime) {
        // todo 校验 refreshToken 是否有效（此处简单校验，实际可结合存储的 refreshToken 进行验证）
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid refreshToken");
        }

        // 生成新的 Token
        return Jwts.builder()
                .setSubject(JWT_SUBJECT)
                .claim(JWT_CLAIM_USER_ID, userInfo.getUserId())
                .claim(JWT_CLAIM_USER_NAME, userInfo.getUserName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}
