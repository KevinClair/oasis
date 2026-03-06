package com.github.kevin.oasis.starter.autoconfigure;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Oasis scheduler starter properties.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "oasis.scheduler")
public class OasisSchedulerProperties {

    private boolean enabled = true;

    @NotBlank
    private String appCode;

    @NotBlank
    private String appKey;

    private Admin admin = new Admin();

    private Server server = new Server();

    private Heartbeat heartbeat = new Heartbeat();

    private Worker worker = new Worker();

    private Callback callback = new Callback();

    private Security security = new Security();

    @Data
    public static class Admin {
        @NotBlank
        private String baseUrl = "http://127.0.0.1:8080";

        @Min(100)
        private int connectTimeoutMs = 2000;

        @Min(100)
        private int readTimeoutMs = 3000;
    }

    @Data
    public static class Server {
        @Min(1)
        private int port = 19091;

        private String contextPath = "/oasis-executor";
    }

    @Data
    public static class Heartbeat {
        @Min(500)
        private long intervalMs = 2000;

        @Min(1)
        private int missThreshold = 2;
    }

    @Data
    public static class Worker {
        @Min(1)
        private int coreSize = 4;

        @Min(1)
        private int maxSize = 16;

        @Min(1)
        private int queueCapacity = 1000;
    }

    @Data
    public static class Callback {
        @Min(1)
        private int batchSize = 100;

        @Min(100)
        private long flushIntervalMs = 1000;
    }

    @Data
    public static class Security {
        private String signAlgorithm = "HMAC-SHA256";

        @Min(0)
        private long clockSkewMs = 30000;
    }
}
