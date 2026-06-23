package com.github.kevin.oasis.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码工具类。
 * 使用 BCryptPasswordEncoder 进行密码哈希和校验。
 */
public final class BCryptUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private BCryptUtil() {
    }

    /**
     * 对明文密码进行 BCrypt 哈希。
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码是否匹配 BCrypt 哈希。
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
