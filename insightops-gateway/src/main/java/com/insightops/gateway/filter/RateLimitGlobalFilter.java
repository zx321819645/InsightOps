package com.insightops.gateway.filter;

import com.insightops.gateway.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 基于 Redis 的固定窗口限流过滤器。
 * <p>
 * 优先按登录用户 ID（{@code X-User-Id}）限流，未登录则按客户端 IP 限流。
 * 需在 JWT 鉴权之后执行，以便拿到用户标识。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:gateway:";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final RateLimitProperties rateLimitProperties;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!rateLimitProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        if (!shouldRateLimit(path)) {
            return chain.filter(exchange);
        }

        String limitKey = buildLimitKey(request);
        String redisKey = RATE_LIMIT_KEY_PREFIX + limitKey;

        return reactiveStringRedisTemplate.opsForValue().increment(redisKey)
                .flatMap(count -> {
                    if (count == 1) {
                        return reactiveStringRedisTemplate
                                .expire(redisKey, Duration.ofSeconds(rateLimitProperties.getWindowSeconds()))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > rateLimitProperties.getMaxRequests()) {
                        return tooManyRequests(exchange);
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(ex -> chain.filter(exchange));
    }

    private boolean shouldRateLimit(String path) {
        return rateLimitProperties.getPaths().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private String buildLimitKey(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (StringUtils.hasText(userId)) {
            return "user:" + userId;
        }
        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format(
                "{\"code\":429,\"message\":\"%s\",\"data\":null}",
                "请求过于频繁，请稍后再试");
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在 JWT 鉴权（-100）之后执行，以便使用 X-User-Id
        return -90;
    }
}
