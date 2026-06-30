package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * {@code queryLogs} Tool 的入参模型（解析默认值与校验后的有效参数）。
 * <p>
 * 由 {@link com.insightops.agent.tool.mock.MockLogsTool} 在参数校验后构建，
 * 便于 audit-service 记录入参快照，并与 {@link LogsQueryData} 出参对照。
 * 结构定义见 docs/tool-spi.md §7.2。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogsQueryParams {

    /** 目标微服务名称，必填，如 order-service */
    private String serviceName;

    /** 查询时间范围，键为 start / end，如 -15m、now */
    private Map<String, String> timeRange;

    /** 关键词过滤，可为 null 表示不过滤 */
    private String keyword;

    /** 日志级别：DEBUG / INFO / WARN / ERROR */
    private String level;

    /** 返回条数上限 */
    private Integer limit;
}
