package com.insightops.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT 鉴权相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "insightops.jwt")
public class JwtProperties {

    /** JWT 签名密钥，需与 auth-service 保持一致 */
    private String secret = "zx040106";

    /** 免登录白名单路径（Ant 风格，如 /auth/auth/login） */
    private List<String> whitelist = new ArrayList<>();
}
