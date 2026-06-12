package com.insightops.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关限流配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "insightops.rate-limit")
public class RateLimitProperties {

    /** 是否启用限流 */
    private boolean enabled = true;

    /** 时间窗口（秒） */
    private int windowSeconds = 60;

    /** 窗口内最大请求数 */
    private int maxRequests = 30;

    /** 需要限流的路径（Ant 风格） */
    private List<String> paths = new ArrayList<>();
}
