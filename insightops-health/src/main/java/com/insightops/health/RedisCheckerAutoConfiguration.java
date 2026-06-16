package com.insightops.health;

import com.insightops.health.checker.RedisMiddlewareChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.data.redis.connection.RedisConnectionFactory")
public class RedisCheckerAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public MiddlewareChecker redisMiddlewareChecker(RedisConnectionFactory redisConnectionFactory) {
        return new RedisMiddlewareChecker(redisConnectionFactory);
    }
}
