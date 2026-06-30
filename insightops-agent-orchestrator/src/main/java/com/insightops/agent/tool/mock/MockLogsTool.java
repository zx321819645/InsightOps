package com.insightops.agent.tool.mock;

import com.insightops.agent.tool.ToolResult;
import com.insightops.agent.tool.dto.LogEntry;
import com.insightops.agent.tool.dto.LogsQueryData;
import com.insightops.agent.tool.dto.LogsQueryParams;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@code queryLogs} 的 Week 3 Mock 实现。
 * <p>
 * 不连接 log-adapter / ELK，返回固定的 Redis 连接超时 ERROR 日志，
 * 时间线与 {@link MockMetricsTool} 劣化点（14:30）对齐。
 * Week 7 将替换为真实 adapter 调用，对外入参/出参 Schema 不变。
 * </p>
 */
@Component
public class MockLogsTool {

    public static final String TOOL_NAME = "queryLogs";

    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9-]*$");

    private static final Set<String> ALLOWED_LEVELS = Set.of("DEBUG", "INFO", "WARN", "ERROR");

    private static final String DEFAULT_START_TIME = "-15m";
    private static final String DEFAULT_END_TIME = "now";
    private static final String DEFAULT_LEVEL = "ERROR";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int MAX_KEYWORD_LENGTH = 256;

    /**
     * 执行 Mock 日志查询。
     *
     * @param serviceName 目标微服务名，必填
     * @param startTime   查询起始时间，空则默认 -15m
     * @param endTime     查询结束时间，空则默认 now
     * @param keyword     关键词过滤，空则不过滤
     * @param level       日志级别，空则默认 ERROR
     * @param limit       返回条数上限，空则默认 20
     */
    public ToolResult query(String serviceName, String startTime, String endTime,
                            String keyword, String level, Integer limit) {
        long startMs = System.currentTimeMillis();

        if (!StringUtils.hasText(serviceName)) {
            return ToolResult.validationError(TOOL_NAME, "serviceName 不能为空", elapsed(startMs));
        }

        String normalizedService = serviceName.trim();
        if (!SERVICE_NAME_PATTERN.matcher(normalizedService).matches()) {
            return ToolResult.validationError(TOOL_NAME,
                    "serviceName 格式不合法，只允许小写字母、数字和连字符，且以小写字母开头，如 order-service",
                    elapsed(startMs));
        }

        String resolvedStart = StringUtils.hasText(startTime) ? startTime.trim() : DEFAULT_START_TIME;
        String resolvedEnd = StringUtils.hasText(endTime) ? endTime.trim() : DEFAULT_END_TIME;
        String resolvedLevel = StringUtils.hasText(level) ? level.trim().toUpperCase(Locale.ROOT) : DEFAULT_LEVEL;
        if (!ALLOWED_LEVELS.contains(resolvedLevel)) {
            return ToolResult.validationError(TOOL_NAME,
                    "level 必须是 DEBUG、INFO、WARN、ERROR 之一",
                    elapsed(startMs));
        }

        String resolvedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        if (resolvedKeyword != null && resolvedKeyword.length() > MAX_KEYWORD_LENGTH) {
            return ToolResult.validationError(TOOL_NAME,
                    "keyword 长度不能超过 " + MAX_KEYWORD_LENGTH + " 个字符",
                    elapsed(startMs));
        }

        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedLimit < 1 || resolvedLimit > MAX_LIMIT) {
            return ToolResult.validationError(TOOL_NAME,
                    "limit 必须在 1 到 " + MAX_LIMIT + " 之间",
                    elapsed(startMs));
        }

        LogsQueryParams params = LogsQueryParams.builder()
                .serviceName(normalizedService)
                .timeRange(Map.of("start", resolvedStart, "end", resolvedEnd))
                .keyword(resolvedKeyword)
                .level(resolvedLevel)
                .limit(resolvedLimit)
                .build();

        LogsQueryData data = buildMockData(params);
        String summary = buildSummary(normalizedService, data.getTotalMatched());

        return ToolResult.success(TOOL_NAME, summary, data, elapsed(startMs));
    }

    private LogsQueryData buildMockData(LogsQueryParams params) {
        List<LogEntry> allEntries = List.of(
                LogEntry.builder()
                        .timestamp("2026-06-23T14:30:12+08:00")
                        .level("ERROR")
                        .message("Redis connection timeout after 3000ms")
                        .traceId("trace-order-abc123")
                        .stackTrace("com.insightops.order.cache.RedisClientException: timeout after 3000ms"
                                + "\n\tat com.insightops.order.cache.RedisClient.get(RedisClient.java:87)")
                        .build(),
                LogEntry.builder()
                        .timestamp("2026-06-23T14:30:15+08:00")
                        .level("ERROR")
                        .message("Failed to get cache key: order:12345, cause: Redis timeout")
                        .traceId("trace-order-def456")
                        .stackTrace("java.util.concurrent.TimeoutException: Waited 3000ms for Redis response"
                                + "\n\tat com.insightops.order.service.OrderService.loadOrder(OrderService.java:142)")
                        .build(),
                LogEntry.builder()
                        .timestamp("2026-06-23T14:30:18+08:00")
                        .level("ERROR")
                        .message("HikariPool connection slow, possible downstream Redis delay")
                        .traceId("trace-order-ghi789")
                        .stackTrace(null)
                        .build()
        );

        List<LogEntry> filtered = allEntries.stream()
                .filter(entry -> params.getLevel().equals(entry.getLevel()))
                .filter(entry -> matchesKeyword(entry, params.getKeyword()))
                .limit(params.getLimit())
                .collect(Collectors.toList());

        return LogsQueryData.builder()
                .query(params)
                .serviceName(params.getServiceName())
                .totalMatched(filtered.size())
                .entries(filtered)
                .build();
    }

    private boolean matchesKeyword(LogEntry entry, String keyword) {
        if (keyword == null) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return entry.getMessage().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || (entry.getStackTrace() != null
                && entry.getStackTrace().toLowerCase(Locale.ROOT).contains(lowerKeyword));
    }

    private String buildSummary(String serviceName, int totalMatched) {
        return serviceName + " 在时间范围内共匹配 " + totalMatched + " 条 ERROR 日志，"
                + "均为 Redis 连接超时（3000ms），集中在 14:30 左右，与 P99 劣化时间一致，"
                + "建议进一步检索 Redis 相关运维知识或检查 Redis 连通性与连接池配置";
    }

    private long elapsed(long startMs) {
        return System.currentTimeMillis() - startMs;
    }
}
