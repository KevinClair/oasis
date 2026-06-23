package com.github.kevin.oasis.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HmacSignatureUtilTest {

    @Test
    void signAndVerify() {
        String secret = "test-secret-key-123";
        long ts = System.currentTimeMillis();
        String nonce = "abc123";
        String method = "POST";
        String path = "/executor/registry/heartbeat";
        String body = "{\"appCode\":\"test\"}";

        String sig = HmacSignatureUtil.sign(secret, ts, nonce, method, path, body);
        assertNotNull(sig);
        assertFalse(sig.isEmpty());

        // Same inputs produce same signature
        String sig2 = HmacSignatureUtil.sign(secret, ts, nonce, method, path, body);
        assertEquals(sig, sig2);
    }

    @Test
    void differentBodyProducesDifferentSignature() {
        String secret = "key";
        long ts = 1000L;
        String sig1 = HmacSignatureUtil.sign(secret, ts, "n", "POST", "/p", "{\"a\":1}");
        String sig2 = HmacSignatureUtil.sign(secret, ts, "n", "POST", "/p", "{\"a\":2}");
        assertNotEquals(sig1, sig2);
    }

    @Test
    void constantTimeEqualsWorks() {
        assertTrue(HmacSignatureUtil.constantTimeEquals("abc", "abc"));
        assertFalse(HmacSignatureUtil.constantTimeEquals("abc", "abd"));
        assertFalse(HmacSignatureUtil.constantTimeEquals(null, "abc"));
        assertFalse(HmacSignatureUtil.constantTimeEquals("abc", null));
    }
}
