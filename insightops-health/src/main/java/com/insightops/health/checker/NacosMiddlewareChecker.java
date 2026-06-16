package com.insightops.health.checker;

import com.insightops.health.MiddlewareCheckResult;
import com.insightops.health.MiddlewareChecker;
import com.insightops.health.NacosHealthIndicator;

public class NacosMiddlewareChecker implements MiddlewareChecker {

    private final NacosHealthIndicator nacosHealthIndicator;

    public NacosMiddlewareChecker(NacosHealthIndicator nacosHealthIndicator) {
        this.nacosHealthIndicator = nacosHealthIndicator;
    }

    @Override
    public String label() {
        return "Nacos 注册中心";
    }

    @Override
    public MiddlewareCheckResult check() {
        var health = nacosHealthIndicator.health();
        if (health.getStatus().getCode().equals("UP")) {
            Object serverAddr = health.getDetails().get("serverAddr");
            return MiddlewareCheckResult.ok(serverAddr != null ? "serverAddr=" + serverAddr : null);
        }
        return MiddlewareCheckResult.fail(String.valueOf(health.getDetails()));
    }
}
