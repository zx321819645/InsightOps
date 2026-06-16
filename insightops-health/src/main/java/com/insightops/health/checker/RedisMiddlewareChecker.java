package com.insightops.health.checker;

import com.insightops.health.MiddlewareCheckResult;
import com.insightops.health.MiddlewareChecker;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public class RedisMiddlewareChecker implements MiddlewareChecker {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisMiddlewareChecker(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public String label() {
        return "Redis";
    }

    @Override
    public MiddlewareCheckResult check() {
        try (var connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            return MiddlewareCheckResult.ok("PING -> " + pong);
        } catch (Exception ex) {
            return MiddlewareCheckResult.fail(ex.getMessage());
        }
    }
}
