package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * {@code queryMetrics} Tool 的业务载荷（{@link com.insightops.agent.tool.ToolResult#getData()}）。
 * <p>
 * 结构定义见 docs/tool-spi.md §7.1；Week 7 接入 monitor-adapter 后，
 * 由 Prometheus 查询结果转换为本对象，Agent 层 Schema 不变。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsQueryData {

    /** 被查询的微服务名称 */
    private String serviceName;

    /** 实际查询的时间范围，键为 start / end */
    private Map<String, String> timeRange;

    /** 用户请求的指标名列表，如 p99、error_rate */
    private List<String> metrics;

    /** 按时间排序的指标采样点 */
    private List<MetricPoint> points;
}
