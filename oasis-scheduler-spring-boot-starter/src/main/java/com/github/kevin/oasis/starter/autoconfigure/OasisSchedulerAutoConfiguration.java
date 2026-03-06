package com.github.kevin.oasis.starter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Starter auto-configuration.
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(OasisSchedulerProperties.class)
@ConditionalOnProperty(prefix = "oasis.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OasisSchedulerAutoConfiguration {

    @Bean
    public RestTemplate oasisRestTemplate(OasisSchedulerProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getAdmin().getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getAdmin().getReadTimeoutMs());
        return new RestTemplate(requestFactory);
    }
}
