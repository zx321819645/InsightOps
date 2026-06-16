package com.insightops.health;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Nacos 注册中心连通性探针。
 */
public class NacosHealthIndicator implements HealthIndicator {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    public NacosHealthIndicator(NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    @Override
    public Health health() {
        String serverAddr = nacosDiscoveryProperties.getServerAddr();
        if (!StringUtils.hasText(serverAddr)) {
            return Health.unknown()
                    .withDetail("reason", "spring.cloud.nacos.discovery.server-addr 未配置")
                    .build();
        }

        String normalizedAddr = serverAddr.replaceFirst("^https?://", "");
        String url = "http://" + normalizedAddr + "/nacos/v1/console/health/readiness";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null && response.body().contains("OK")) {
                return Health.up().withDetail("serverAddr", serverAddr).build();
            }
            return Health.down()
                    .withDetail("serverAddr", serverAddr)
                    .withDetail("httpStatus", response.statusCode())
                    .withDetail("body", response.body())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("serverAddr", serverAddr).build();
        }
    }
}
