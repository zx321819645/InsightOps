package com.insightops.health;

/**
 * 中间件连接探针，由各服务按 classpath 条件注册。
 */
public interface MiddlewareChecker {

    String label();

    MiddlewareCheckResult check();
}
