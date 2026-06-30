package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code searchKnowledge} Tool 的入参模型（解析默认值与校验后的有效参数）。
 * <p>
 * 由 {@link com.insightops.agent.tool.mock.MockKnowledgeTool} 在参数校验后构建，
 * 便于 audit-service 记录入参快照。结构定义见 docs/tool-spi.md §7.3。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchParams {

    /** 自然语言检索词，必填，如 Redis connection timeout 处理 */
    private String query;

    /** 返回文档片段数量上限 */
    private Integer topK;

    /** 文档分类过滤：incident / runbook / faq，null 表示不过滤 */
    private String category;
}
