package com.insightops.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * Gateway 响应式 Redis 配置（WebFlux 环境专用）。
 * <p>
 * 连接参数由 {@code spring.data.redis.*} 自动装配，限流过滤器使用 {@link ReactiveStringRedisTemplate}。
 * </p>
 */
@Configuration
public class ReactiveRedisConfig {

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }
}
