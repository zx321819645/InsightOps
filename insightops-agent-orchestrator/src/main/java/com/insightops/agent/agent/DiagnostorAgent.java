package com.insightops.agent.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 诊断 Agent 接口（LangChain4j AiServices 代理目标）。
 * <p>
 * <b>初学者要点：</b>
 * <ul>
 *   <li>这是一个<strong>接口</strong>，不需要写实现类</li>
 *   <li>LangChain4j 的 {@code AiServices.builder(DiagnostorAgent.class).build()} 会自动生成实现</li>
 *   <li>{@code @SystemMessage} = 系统 Prompt，告诉 LLM「你是谁、怎么工作」</li>
 *   <li>{@code @UserMessage} = 用户输入，每次 diagnose 调用时传入</li>
 *   <li>Tool 不在此接口声明，而是在 {@code DiagnostorAgentServiceImpl} 里通过 {@code .tools(opsTools)} 绑定</li>
 * </ul>
 * </p>
 *
 * <pre>
 * 调用链示意：
 *   Controller → ServiceImpl.diagnose("order-service 变慢")
 *       → agent.diagnose(...)   // LangChain4j 生成的代理
 *       → LLM 思考 → 调 queryMetrics → 观察 → 调 queryLogs → ... → 返回 String 结论
 * </pre>
 */
public interface DiagnostorAgent {

    /**
     * 对运维故障问题进行 ReAct 诊断。
     * <p>
     * {@code @SystemMessage(fromResource = "...")} 从 classpath 加载 prompts/diagnostor-system.txt；
     * 请先在该文件中写好运维专家角色与输出格式（任务 3.4）。
     * </p>
     *
     * @param userQuestion 用户问题，由 {@code @UserMessage} 标记为「用户消息」发给 LLM
     * @return Agent 最终诊断报告（LLM 在调完 Tool 后生成的文本）
     */
    @SystemMessage(fromResource = "prompts/diagnostor-system.txt")
    String diagnose(@UserMessage String userQuestion);

    /*
     * ========== 注解说明（参考） ==========
     *
     * 1. @SystemMessage(fromResource = "...")
     *    - 从 resources 目录加载系统 Prompt
     *    - 等价于 Chat 里的 system 角色消息
     *
     * 2. @UserMessage（标注在参数上）
     *    - 把 userQuestion 作为「用户消息」发给 LLM
     *    - 每次调用 diagnose("order-service 变慢") 时，这句话就是 user 角色输入
     *
     * 3. 若暂时不想用外部文件，可把 @SystemMessage 改成内联字符串（见下方注释掉的示例）
     *
     * 4. 返回 String = Agent 最终回复；中间 Tool 调用在 ServiceImpl 里 .tools(opsTools) 绑定，无需在此声明
     */

    /*
    // --- 内联 Prompt 写法示例（调试时可临时替换上面的 fromResource）---
    @SystemMessage("""
        你是 AIOps 运维专家。诊断顺序：queryMetrics → queryLogs → searchKnowledge。
        仅基于 Tool 返回数据下结论。输出：现象、根因、证据、建议。
        """)
    String diagnoseInline(@UserMessage String userQuestion);
    */
}
