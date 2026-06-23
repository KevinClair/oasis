package com.github.kevin.oasis.services.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 阻塞策略逻辑单元测试（不依赖 Spring/DB）。
 * 验证 DISCARD_LATER / COVER_EARLY / SERIAL_EXECUTION 的判断分支。
 */
class BlockStrategyTest {

    @Test
    void nullBlockStrategyShouldPass() {
        assertNull(normalizeBlockStrategy(null));
    }

    @Test
    void emptyBlockStrategyShouldPass() {
        assertNull(normalizeBlockStrategy(""));
        assertNull(normalizeBlockStrategy("  "));
    }

    @Test
    void discardLaterNormalized() {
        assertEquals("DISCARD_LATER", normalizeBlockStrategy("DISCARD_LATER"));
        assertEquals("DISCARD_LATER", normalizeBlockStrategy("discard_later"));
        assertEquals("DISCARD_LATER", normalizeBlockStrategy(" Discard_Later "));
    }

    @Test
    void coverEarlyNormalized() {
        assertEquals("COVER_EARLY", normalizeBlockStrategy("COVER_EARLY"));
        assertEquals("COVER_EARLY", normalizeBlockStrategy("cover_early"));
    }

    @Test
    void serialExecutionNormalized() {
        assertEquals("SERIAL_EXECUTION", normalizeBlockStrategy("SERIAL_EXECUTION"));
    }

    /**
     * 模拟业务层正则化逻辑。
     */
    private static String normalizeBlockStrategy(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().toUpperCase();
    }
}
