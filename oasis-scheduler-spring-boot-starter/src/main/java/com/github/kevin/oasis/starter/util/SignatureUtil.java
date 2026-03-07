package com.github.kevin.oasis.starter.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * HMAC signature utility.
 */
public final class SignatureUtil {

    private SignatureUtil() {
    }

    public static String nonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String sign(String secret, long timestamp, String nonce, String method, String path, String body) {
        try {
            String bodyHash = sha256Hex(body == null ? "" : body);
            String payload = timestamp + "\n" + nonce + "\n" + method + "\n" + path + "\n" + bodyHash;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signed = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signed);
        } catch (Exception e) {
            throw new IllegalStateException("build signature failed", e);
        }
    }

    public static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String value) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
