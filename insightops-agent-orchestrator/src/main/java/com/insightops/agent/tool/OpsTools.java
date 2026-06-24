package com.insightops.agent.tool;

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
}
