package com.insightops.health;

/**
 * 单个中间件的连接检查结果。
 */
public record MiddlewareCheckResult(boolean success, String detail) {

    public static MiddlewareCheckResult ok(String detail) {
        return new MiddlewareCheckResult(true, detail);
    }

    public static MiddlewareCheckResult fail(String detail) {
        return new MiddlewareCheckResult(false, detail);
    }
}
