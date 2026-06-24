
package com.insightops.agent.tool.mock;

import com.insightops.agent.tool.ToolResult;
import com.insightops.agent.tool.dto.MetricPoint;
import com.insightops.agent.tool.dto.MetricsQueryData;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * {@code queryMetrics} 的 Week 3 Mock 实现。
 * <p>
 * 不连接 Prometheus / monitor-adapter，返回固定的「性能劣化」演示数据，
 * 用于验收场景：「order-service 响应变慢」→ Agent 调用本 Tool 发现 P99 升高。
 * Week 7 将替换为真实 adapter 调用，对外入参/出参 Schema 不变。
 * </p>
 */
@Component
public class MockMetricsTool {

    /** Tool 全局唯一名称，与 OpsTools.queryMetrics 及 audit 记录保持一致 */
    public static final String TOOL_NAME = "queryMetrics";

    /** 微服务名格式：小写字母开头，仅允许小写字母、数字、连字符 */
    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9-]*$");

    private static final String DEFAULT_START_TIME = "-15m";
    private static final String DEFAULT_END_TIME = "now";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private static final List<String> DEFAULT_METRICS = List.of("p99", "error_rate");

    /**
     * 执行 Mock 指标查询。
     *
     * @param serviceName 目标微服务名，必填
     * @param startTime   查询起始时间，空则默认 -15m
     * @param endTime     查询结束时间，空则默认 now
     * @param metrics     指标名列表，空则默认 p99 + error_rate
     * @param limit       返回数据点上限，空则默认 20
     * @return 统一 Tool 出参；参数非法时返回 validationError，不抛异常
     */
    public ToolResult query(String serviceName, String startTime, String endTime,
                            List<String> metrics, Integer limit) {
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
        List<String> resolvedMetrics = (metrics == null || metrics.isEmpty()) ? DEFAULT_METRICS : metrics;

        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedLimit < 1 || resolvedLimit > MAX_LIMIT) {
            return ToolResult.validationError(TOOL_NAME,
                    "limit 必须在 1 到 " + MAX_LIMIT + " 之间",
                    elapsed(startMs));
        }

        MetricsQueryData data = buildMockData(normalizedService, resolvedStart, resolvedEnd, resolvedMetrics);
        String summary = buildSummary(normalizedService);

        return ToolResult.success(TOOL_NAME, summary, data, elapsed(startMs));
    }

    /**
     * 构造 Mock 劣化场景：P99 从 200ms 升至 2.1s，错误率从 0.3% 升至 2.1%。
     * 两个采样点便于 Agent 对比「正常 → 异常」的变化趋势。
     */
    private MetricsQueryData buildMockData(String serviceName, String startTime, String endTime,
                                           List<String> metrics) {
        List<MetricPoint> points = List.of(
                MetricPoint.builder()
                        .time("2026-06-23T14:15:00+08:00")
                        .p99Ms(200)
                        .p95Ms(120)
                        .qps(1180)
                        .errorRate(0.003)
                        .cpuUsage(0.45)
                        .build(),
                MetricPoint.builder()
                        .time("2026-06-23T14:30:00+08:00")
                        .p99Ms(2100)
                        .p95Ms(850)
                        .qps(1200)
                        .errorRate(0.021)
                        .cpuUsage(0.78)
                        .build()
        );

        return MetricsQueryData.builder()
                .serviceName(serviceName)
                .timeRange(Map.of("start", startTime, "end", endTime))
                .metrics(metrics)
                .points(points)
                .build();
    }

    /** 生成面向 LLM 的自然语言摘要 */
    private String buildSummary(String serviceName) {
        return serviceName + " 近 15 分钟 P99 延迟从 200ms 升至 2.1s，错误率从 0.3% 升至 2.1%，"
                + "CPU 使用率从 45% 升至 78%，存在明显性能劣化";
    }

    private long elapsed(long startMs) {
        return System.currentTimeMillis() - startMs;
    }
}
