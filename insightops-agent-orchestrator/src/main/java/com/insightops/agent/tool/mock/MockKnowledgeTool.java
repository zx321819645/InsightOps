package com.insightops.agent.tool.mock;

import com.insightops.agent.tool.ToolResult;
import com.insightops.agent.tool.dto.KnowledgeChunk;
import com.insightops.agent.tool.dto.KnowledgeSearchData;
import com.insightops.agent.tool.dto.KnowledgeSearchParams;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code searchKnowledge} 的 Week 3 Mock 实现。
 * <p>
 * 不连接 knowledge-service / Milvus，返回硬编码的 Redis 连接超时处理文档片段，
 * 与 {@link MockMetricsTool}、{@link MockLogsTool} 的 Demo 剧本对齐。
 * Week 5 将替换为真实 RAG 检索，对外入参/出参 Schema 不变。
 * </p>
 */
@Component
public class MockKnowledgeTool {

    public static final String TOOL_NAME = "searchKnowledge";

    private static final Set<String> ALLOWED_CATEGORIES = Set.of("incident", "runbook", "faq");

    private static final int DEFAULT_TOP_K = 3;
    private static final int MAX_TOP_K = 10;
    private static final int MAX_QUERY_LENGTH = 512;

    /**
     * 执行 Mock 知识库检索。
     *
     * @param query    自然语言检索词，必填
     * @param topK     返回片段数上限，空则默认 3
     * @param category 文档分类过滤，空则不过滤
     */
    public ToolResult search(String query, Integer topK, String category) {
        long startMs = System.currentTimeMillis();

        if (!StringUtils.hasText(query)) {
            return ToolResult.validationError(TOOL_NAME, "query 不能为空", elapsed(startMs));
        }

        String normalizedQuery = query.trim();
        if (normalizedQuery.length() > MAX_QUERY_LENGTH) {
            return ToolResult.validationError(TOOL_NAME,
                    "query 长度不能超过 " + MAX_QUERY_LENGTH + " 个字符",
                    elapsed(startMs));
        }

        int resolvedTopK = topK == null ? DEFAULT_TOP_K : topK;
        if (resolvedTopK < 1 || resolvedTopK > MAX_TOP_K) {
            return ToolResult.validationError(TOOL_NAME,
                    "topK 必须在 1 到 " + MAX_TOP_K + " 之间",
                    elapsed(startMs));
        }

        String resolvedCategory = null;
        if (StringUtils.hasText(category)) {
            resolvedCategory = category.trim().toLowerCase(Locale.ROOT);
            if (!ALLOWED_CATEGORIES.contains(resolvedCategory)) {
                return ToolResult.validationError(TOOL_NAME,
                        "category 必须是 incident、runbook、faq 之一",
                        elapsed(startMs));
            }
        }

        KnowledgeSearchParams params = KnowledgeSearchParams.builder()
                .query(normalizedQuery)
                .topK(resolvedTopK)
                .category(resolvedCategory)
                .build();

        KnowledgeSearchData data = buildMockData(params);
        String summary = buildSummary(normalizedQuery, data.getTotalMatched(), data.getChunks().size());

        return ToolResult.success(TOOL_NAME, summary, data, elapsed(startMs));
    }

    private KnowledgeSearchData buildMockData(KnowledgeSearchParams params) {
        List<KnowledgeChunk> allChunks = List.of(
                KnowledgeChunk.builder()
                        .docTitle("Redis 连接超时故障手册")
                        .docId("kb-redis-timeout-001")
                        .score(0.92)
                        .category("runbook")
                        .source("knowledge-base")
                        .content("""
                                1. 使用 redis-cli ping 检查 Redis 连通性与网络延迟。
                                2. 检查 Redis maxclients 是否已满：CONFIG GET maxclients。
                                3. 检查应用连接池 max-active、max-wait 配置，避免连接耗尽。
                                4. 查看慢查询与阻塞命令，排查 Big Key 或持久化阻塞。
                                """)
                        .build(),
                KnowledgeChunk.builder()
                        .docTitle("order-service Redis 超时故障案例")
                        .docId("kb-redis-timeout-incident-002")
                        .score(0.85)
                        .category("incident")
                        .source("knowledge-base")
                        .content("""
                                现象：order-service P99 延迟升高，日志出现 Redis connection timeout after 3000ms。
                                根因：连接池 max-wait 过小且 Redis 存在短暂网络抖动。
                                处理：调大 Lettuce 连接池 max-active 至 64，max-wait 至 3000ms，并检查 Redis 网络。
                                """)
                        .build(),
                KnowledgeChunk.builder()
                        .docTitle("Redis maxclients 与连接池 FAQ")
                        .docId("kb-redis-faq-003")
                        .score(0.78)
                        .category("faq")
                        .source("knowledge-base")
                        .content("""
                                Q: 应用报 Redis timeout 但 Redis CPU 不高？
                                A: 常见为连接池耗尽或 maxclients 打满，而非 Redis 自身性能瓶颈。
                                建议先检查连接数与客户端 wait 队列。
                                """)
                        .build()
        );

        List<KnowledgeChunk> matched = allChunks.stream()
                .filter(chunk -> matchesCategory(chunk, params.getCategory()))
                .filter(chunk -> matchesQuery(chunk, params.getQuery()))
                .sorted(Comparator.comparing(KnowledgeChunk::getScore).reversed())
                .collect(Collectors.toList());

        int totalMatched = matched.size();
        List<KnowledgeChunk> limited = matched.stream()
                .limit(params.getTopK())
                .collect(Collectors.toList());

        return KnowledgeSearchData.builder()
                .search(params)
                .query(params.getQuery())
                .totalMatched(totalMatched)
                .chunks(limited)
                .build();
    }

    private boolean matchesCategory(KnowledgeChunk chunk, String category) {
        if (category == null) {
            return true;
        }
        return category.equals(chunk.getCategory());
    }

    /**
     * Mock 相关度：检索词含 redis/timeout/连接/超时 等关键词时命中 Redis 文档；
     * 否则不返回片段（模拟无匹配）。
     */
    private boolean matchesQuery(KnowledgeChunk chunk, String query) {
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        boolean queryRelevant = lowerQuery.contains("redis")
                || lowerQuery.contains("timeout")
                || lowerQuery.contains("连接")
                || lowerQuery.contains("超时")
                || lowerQuery.contains("cache")
                || lowerQuery.contains("缓存");
        if (!queryRelevant) {
            return false;
        }
        String corpus = (chunk.getDocTitle() + " " + chunk.getContent()).toLowerCase(Locale.ROOT);
        return corpus.contains("redis") || corpus.contains("timeout") || corpus.contains("连接");
    }

    private String buildSummary(String query, int totalMatched, int returnedCount) {
        if (totalMatched == 0) {
            return "知识库未检索到与「" + query + "」相关的文档片段，"
                    + "请尝试使用日志中的组件名或错误关键词重新检索，如 Redis connection timeout";
        }
        return "知识库检索「" + query + "」命中 " + totalMatched + " 条相关文档，返回前 "
                + returnedCount + " 条，内容涵盖 Redis 连接超时排查步骤与历史故障案例，"
                + "建议检查 maxclients、连接池配置与 Redis 网络连通性";
    }

    private long elapsed(long startMs) {
        return System.currentTimeMillis() - startMs;
    }
}
