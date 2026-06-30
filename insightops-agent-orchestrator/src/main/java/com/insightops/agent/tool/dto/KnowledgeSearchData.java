package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@code searchKnowledge} Tool 的业务载荷（{@link com.insightops.agent.tool.ToolResult#getData()}）。
 * <p>
 * 结构定义见 docs/tool-spi.md §7.3；Week 5 接入 knowledge-service + Milvus 后，
 * 由真实 RAG 检索结果转换为本对象，Agent 层 Schema 不变。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchData {

    /** 实际生效的检索入参（校验与默认值解析后） */
    private KnowledgeSearchParams search;

    /** 回显检索词，与 search.query 一致，便于 LLM 直接阅读 */
    private String query;

    /** 命中片段总数（过滤后、topK 截断前） */
    private Integer totalMatched;

    /** 按相关度排序的文档片段，最多返回 search.topK 条 */
    private List<KnowledgeChunk> chunks;
}
