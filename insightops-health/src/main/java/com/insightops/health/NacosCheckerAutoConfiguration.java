package com.insightops.health;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.insightops.health.checker.NacosMiddlewareChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(NacosDiscoveryProperties.class)
public class NacosCheckerAutoConfiguration {

    @Bean
    @ConditionalOnBean(NacosDiscoveryProperties.class)
    public MiddlewareChecker nacosMiddlewareChecker(NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new NacosMiddlewareChecker(new NacosHealthIndicator(nacosDiscoveryProperties));
    }
}
