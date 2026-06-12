package com.insightops.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 签发配置，需与 Gateway 保持一致
 */
@Data
@Component
@ConfigurationProperties(prefix = "insightops.jwt")
public class JwtProperties {

    private String secret = "zx040106";

    /** Token 有效期（小时） */
    private int expireHours = 24;
}
