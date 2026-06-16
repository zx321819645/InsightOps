package com.insightops.health;

import com.insightops.health.checker.ReactiveRedisMiddlewareChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.data.redis.connection.ReactiveRedisConnectionFactory")
public class ReactiveRedisCheckerAutoConfiguration {

    @Bean
    @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
    public MiddlewareChecker reactiveRedisMiddlewareChecker(
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        return new ReactiveRedisMiddlewareChecker(reactiveRedisConnectionFactory);
    }
}
