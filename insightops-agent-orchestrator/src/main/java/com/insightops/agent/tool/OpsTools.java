package com.insightops.agent.tool;

import com.insightops.agent.tool.mock.MockKnowledgeTool;
import com.insightops.agent.tool.mock.MockLogsTool;
import com.insightops.agent.tool.mock.MockMetricsTool;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent 可调用的运维 Tool 集合（LangChain4j 入口层）。
 * <p>
 * 本类仅负责声明 {@link Tool} 注解及参数描述，供 LLM 理解「何时调用、传什么参数」；
 * 具体执行逻辑委托给 {@code mock} 包下的实现类。Week 3 为 Mock，Week 7 起可替换为远程 adapter。
 * </p>
 * <p>
 * 在 ReAct Agent 中通过 {@code AiServices.builder(...).tools(opsTools)} 注入。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OpsTools {

    private final MockMetricsTool mockMetricsTool;
    private final MockLogsTool mockLogsTool;
    private final MockKnowledgeTool mockKnowledgeTool;

    /**
     * 查询指定微服务在时间范围内的性能指标。
     * <p>
     * <b>何时调用：</b>
     * <ul>
     *   <li>用户反馈某服务「响应变慢、卡顿、超时、延迟高」</li>
     *   <li>用户询问 CPU、内存、QPS、P99/P95、错误率等指标</li>
     *   <li>故障诊断的第一步，先确认是否存在指标层面的异常</li>
     * </ul>
     * <b>调用顺序建议：</b>通常作为诊断链路的第一个 Tool；若发现指标异常，
     * 下一步应调用 queryLogs 查日志定位根因，再调用 searchKnowledge 查处理方案。
     * <b>限制：</b>只读查询，不修改任何系统状态。
     * </p>
     */
    @Tool("""
            查询指定微服务在时间范围内的性能指标，包括 QPS、P99/P95 延迟、CPU 使用率、错误率等。
            当用户描述服务变慢、卡顿、超时、延迟升高、CPU 高、错误率上升时使用。
            这是故障诊断的第一步：先通过指标确认是否存在性能劣化，再决定是否查日志或知识库。
            不要在没有 serviceName 时猜测调用，若用户未指定服务名应先追问或使用上下文中的服务名。
            """)
    public ToolResult queryMetrics(
            @P(value = """
                    目标微服务的名称，必填。
                    格式：小写字母开头，仅含小写字母、数字和连字符。
                    示例：order-service、payment-service、user-service。
                    若用户说「订单服务」，应转换为 order-service 这类标准命名。
                    """)
            String serviceName,

            @P(value = """
                    查询起始时间，可选，默认 -15m（最近 15 分钟）。
                    支持相对时间：-15m、-1h、-30m；或 ISO-8601 绝对时间。
                    诊断「刚刚变慢」的场景建议使用 -15m 或 -30m。
                    """)
            String startTime,

            @P(value = """
                    查询结束时间，可选，默认 now（当前时刻）。
                    支持相对时间 now，或 ISO-8601 绝对时间。
                    与 startTime 配合确定查询窗口，窗口不宜超过 24 小时。
                    """)
            String endTime,

            @P(value = """
                    需要查询的指标名称列表，可选，默认 ["p99", "error_rate"]。
                    常见值：p99、p95、qps、error_rate、cpu、memory。
                    用户关心延迟时优先 p99/p95；关心稳定性时加上 error_rate。
                    """)
            List<String> metrics,

            @P(value = """
                    返回的数据点数量上限，可选，默认 20，最大 100。
                    时间点过多会增加 Token 消耗，一般 10~20 个点足够观察趋势。
                    """)
            Integer limit) {

        return mockMetricsTool.query(serviceName, startTime, endTime, metrics, limit);
    }

    /**
     * 查询指定微服务在时间范围内的应用日志。
     * <p>
     * <b>何时调用：</b>
     * <ul>
     *   <li>queryMetrics 已发现指标异常，需要查 ERROR 日志定位根因</li>
     *   <li>用户询问异常栈、超时原因、具体错误信息</li>
     *   <li>需要从日志中提取组件名（如 Redis、MySQL）供 searchKnowledge 检索</li>
     * </ul>
     * <b>调用顺序建议：</b>在 queryMetrics 之后、searchKnowledge 之前调用。
     * <b>限制：</b>只读查询，不修改任何系统状态。
     * </p>
     */
    @Tool("""
            查询指定微服务在时间范围内的应用日志，支持按关键词、日志级别、时间范围过滤。
            当 queryMetrics 发现性能劣化或错误率升高后，使用本 Tool 排查异常栈、错误信息与超时原因。
            若日志中出现 Redis、MySQL、Kafka 等组件名，应提取后调用 searchKnowledge 查询处理方案。
            不要在尚未确认指标异常时跳过 queryMetrics 直接查日志（除非用户明确要求只看日志）。
            """)
    public ToolResult queryLogs(
            @P(value = """
                    目标微服务的名称，必填。
                    格式：小写字母开头，仅含小写字母、数字和连字符。
                    示例：order-service、payment-service。
                    应与 queryMetrics 使用相同的 serviceName，以保持诊断链路一致。
                    """)
            String serviceName,

            @P(value = """
                    查询起始时间，可选，默认 -15m（最近 15 分钟）。
                    建议与 queryMetrics 使用相同时间窗口，便于将日志与指标劣化时间点对齐。
                    支持相对时间 -15m、-1h 或 ISO-8601 绝对时间。
                    """)
            String startTime,

            @P(value = """
                    查询结束时间，可选，默认 now（当前时刻）。
                    与 startTime 配合确定查询窗口。
                    """)
            String endTime,

            @P(value = """
                    日志内容关键词过滤，可选，不传则返回该级别下所有匹配日志。
                    示例：Redis、TimeoutException、connection timeout。
                    从指标异常怀疑某组件时，可传入组件名缩小范围。
                    """)
            String keyword,

            @P(value = """
                    日志级别过滤，可选，默认 ERROR。
                    可选值：DEBUG、INFO、WARN、ERROR。
                    故障诊断场景建议先用 ERROR；需要上下文时可降为 WARN。
                    """)
            String level,

            @P(value = """
                    返回日志条数上限，可选，默认 20，最大 100。
                    日志条数过多会消耗 Token，一般 5~10 条 ERROR 足够定位根因。
                    """)
            Integer limit) {

        return mockLogsTool.query(serviceName, startTime, endTime, keyword, level, limit);
    }

    /**
     * 从运维知识库检索与故障、组件、操作手册相关的文档片段。
     * <p>
     * <b>何时调用：</b>
     * <ul>
     *   <li>queryLogs 已定位到具体错误类型或组件（如 Redis timeout）</li>
     *   <li>需要标准修复步骤、操作手册、历史故障案例</li>
     *   <li>用户明确询问「怎么处理」「有没有手册」</li>
     * </ul>
     * <b>调用顺序建议：</b>在 queryMetrics、queryLogs 之后，作为诊断链路最后一步。
     * <b>限制：</b>只读检索，不修改知识库。
     * </p>
     */
    @Tool("""
            从运维知识库检索与故障、组件、操作手册相关的文档片段，返回可操作的修复建议与案例。
            当 queryLogs 发现具体错误（如 Redis connection timeout）后，根据组件名和错误类型构造 query 调用本 Tool。
            这是故障诊断的第三步：在前两步确认「指标异常 + 日志根因」后，查询标准处理步骤。
            query 应来自日志/指标中的真实线索，不要编造检索词；若尚无日志线索，应先调用 queryLogs。
            """)
    public ToolResult searchKnowledge(
            @P(value = """
                    自然语言检索词，必填。
                    根据 queryLogs 发现的组件与错误类型构造，如「Redis connection timeout 处理」「Redis 连接超时」。
                    可包含组件名（Redis、MySQL、Kafka）和故障现象（timeout、连接失败、慢查询）。
                    """)
            String query,

            @P(value = """
                    返回文档片段数量上限，可选，默认 3，最大 10。
                    片段过多会增加 Token 消耗，一般 2~3 条 runbook + incident 案例足够生成修复建议。
                    """)
            Integer topK,

            @P(value = """
                    文档分类过滤，可选，不传则检索全部分类。
                    可选值：incident（故障案例）、runbook（操作手册）、faq（常见问题）。
                    需要标准处理步骤时优先 runbook；需要类似案例时选 incident。
                    """)
            String category) {

        return mockKnowledgeTool.search(query, topK, category);
    }
}
