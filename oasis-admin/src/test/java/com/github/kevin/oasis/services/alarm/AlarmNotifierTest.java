package com.github.kevin.oasis.services.alarm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlarmNotifierTest {

    private AlarmNotifierRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AlarmNotifierRegistry(List.of(
                new FakeNotifier("CONSOLE"),
                new FakeNotifier("EMAIL"),
                new FakeNotifier("WECHAT")
        ));
    }

    @Test
    void getKnownChannel() {
        assertNotNull(registry.get("CONSOLE"));
        assertNotNull(registry.get("EMAIL"));
    }

    @Test
    void unknownChannelReturnsNull() {
        assertNull(registry.get("SLACK"));
        assertNull(registry.get(null));
    }

    @Test
    void caseInsensitiveChannelName() {
        assertNotNull(registry.get("console"));
        assertNotNull(registry.get(" Console "));
    }

    @Test
    void sendThroughNotifier() {
        AlarmNotifier notifier = registry.get("EMAIL");
        assertTrue(notifier.send("admin@test.com", "Test Alarm", "Job failed"));
    }

    record FakeNotifier(String channel) implements AlarmNotifier {
        @Override
        public boolean send(String receiver, String title, String content) {
            return true;
        }
    }
}
