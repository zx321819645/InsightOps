package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@code queryLogs} Tool 的业务载荷（{@link com.insightops.agent.tool.ToolResult#getData()}）。
 * <p>
 * 结构定义见 docs/tool-spi.md §7.2；Week 7 接入 log-adapter 后，
 * 由真实日志查询结果转换为本对象，Agent 层 Schema 不变。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogsQueryData {

    /** 实际生效的查询入参（校验与默认值解析后） */
    private LogsQueryParams query;

    /** 被查询的微服务名称 */
    private String serviceName;

    /** 命中日志总条数（Mock 或真实查询统计） */
    private Integer totalMatched;

    /** 按时间排序的日志条目，最多返回 query.limit 条 */
    private List<LogEntry> entries;
}
