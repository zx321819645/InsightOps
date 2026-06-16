package com.insightops.health;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

/**
 * 中间件健康检查自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(MiddlewareHealthProperties.class)
public class InsightOpsHealthAutoConfiguration {

    @Bean
    @ConditionalOnClass(NacosDiscoveryProperties.class)
    @ConditionalOnBean(NacosDiscoveryProperties.class)
    public HealthIndicator nacos(NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new NacosHealthIndicator(nacosDiscoveryProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "insightops.health", name = "startup-log-enabled", havingValue = "true", matchIfMissing = true)
    public MiddlewareConsoleReporter middlewareConsoleReporter(MiddlewareHealthProperties properties,
                                                               ObjectProvider<MiddlewareChecker> checkerProvider) {
        return new MiddlewareConsoleReporter(properties, checkerProvider);
    }
}
