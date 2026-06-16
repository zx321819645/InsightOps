package com.insightops.health.checker;

import com.insightops.health.MiddlewareCheckResult;
import com.insightops.health.MiddlewareChecker;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

import java.time.Duration;

public class ReactiveRedisMiddlewareChecker implements MiddlewareChecker {

    private final ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    public ReactiveRedisMiddlewareChecker(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        this.reactiveRedisConnectionFactory = reactiveRedisConnectionFactory;
    }

    @Override
    public String label() {
        return "Redis";
    }

    @Override
    public MiddlewareCheckResult check() {
        try {
            String pong = reactiveRedisConnectionFactory.getReactiveConnection()
                    .ping()
                    .block(Duration.ofSeconds(3));
            return MiddlewareCheckResult.ok("PING -> " + pong);
        } catch (Exception ex) {
            return MiddlewareCheckResult.fail(ex.getMessage());
        }
    }
}
