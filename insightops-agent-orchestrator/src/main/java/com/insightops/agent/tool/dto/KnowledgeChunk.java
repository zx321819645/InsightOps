package com.insightops.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库检索命中的单个文档片段。
 * <p>
 * 对应 {@code searchKnowledge} Tool 出参 {@code data.chunks[]} 中的元素，
 * 结构定义见 docs/tool-spi.md §7.3。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeChunk {

    /** 文档标题，如 Redis 连接超时故障手册 */
    private String docTitle;

    /** 文档唯一 ID，Week 5 与 MySQL 知识库表关联 */
    private String docId;

    /** 相关度评分 0~1，Mock 固定值；Week 5 来自 Milvus 向量相似度 */
    private Double score;

    /** 文档片段正文，含可操作的修复步骤 */
    private String content;

    /** 来源标识，如 knowledge-base */
    private String source;

    /** 文档分类：incident / runbook / faq */
    private String category;
}
