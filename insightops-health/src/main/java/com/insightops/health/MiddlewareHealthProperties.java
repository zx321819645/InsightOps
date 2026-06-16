package com.insightops.health;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 中间件健康检查配置。
 */
@Data
@ConfigurationProperties(prefix = "insightops.health")
public class MiddlewareHealthProperties {

    /**
     * 启动完成后是否在日志中输出各中间件连接结果。
     */
    private boolean startupLogEnabled = true;
}
