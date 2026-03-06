package com.github.kevin.oasis.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 调度运行期配置装配
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SchedulerRuntimeProperties.class)
public class SchedulerRuntimeConfig {
}
