package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个时间点的性能指标采样。
 * <p>
 * 对应 {@code queryMetrics} Tool 出参 {@code data.points[]} 中的元素，
 * 字段命名与 tool-spi.md §7.1 保持一致，Week 7 对接 Prometheus 时可做映射。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricPoint {

    /** 采样时间，ISO-8601 格式，如 2026-06-23T14:30:00+08:00 */
    private String time;

    /** P99 延迟（毫秒） */
    private Integer p99Ms;

    /** P95 延迟（毫秒） */
    private Integer p95Ms;

    /** 每秒请求数（Queries Per Second） */
    private Integer qps;

    /** 错误率，0.021 表示 2.1% */
    private Double errorRate;

    /** CPU 使用率，0.78 表示 78% */
    private Double cpuUsage;
}
