package com.github.kevin.oasis.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BCryptUtilTest {

    @Test
    void encodeAndMatch() {
        String raw = "myPassword123";
        String encoded = BCryptUtil.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded);
        assertTrue(BCryptUtil.matches(raw, encoded));
    }

    @Test
    void wrongPasswordShouldNotMatch() {
        String encoded = BCryptUtil.encode("correct");
        assertFalse(BCryptUtil.matches("wrong", encoded));
    }

    @Test
    void nullInputShouldNotMatch() {
        assertFalse(BCryptUtil.matches(null, "hash"));
        assertFalse(BCryptUtil.matches("raw", null));
        assertFalse(BCryptUtil.matches(null, null));
    }

    @Test
    void eachEncodeProducesDifferentHash() {
        String raw = "same";
        String h1 = BCryptUtil.encode(raw);
        String h2 = BCryptUtil.encode(raw);
        assertNotEquals(h1, h2); // Different salts
        assertTrue(BCryptUtil.matches(raw, h1));
        assertTrue(BCryptUtil.matches(raw, h2));
    }
}
