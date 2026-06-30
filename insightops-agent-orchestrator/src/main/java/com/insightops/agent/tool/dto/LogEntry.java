package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条应用日志记录。
 * <p>
 * 对应 {@code queryLogs} Tool 出参 {@code data.entries[]} 中的元素，
 * 结构定义见 docs/tool-spi.md §7.2。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    /** 日志时间，ISO-8601 格式，如 2026-06-23T14:30:12+08:00 */
    private String timestamp;

    /** 日志级别：DEBUG / INFO / WARN / ERROR */
    private String level;

    /** 日志正文摘要，如 Redis connection timeout after 3000ms */
    private String message;

    /** 分布式链路追踪 ID，便于与 SkyWalking / Gateway traceId 关联 */
    private String traceId;

    /** 异常堆栈，可为 null（非异常日志时） */
    private String stackTrace;
}
