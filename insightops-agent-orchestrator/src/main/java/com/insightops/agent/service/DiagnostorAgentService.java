package com.insightops.agent.service;

/**
 * 诊断 Agent 业务接口（Spring Service 层）。
 * <p>
 * <b>作用：</b>对 Controller 暴露诊断能力，屏蔽 LangChain4j AiServices 构建细节。
 * Controller 只依赖本接口，不直接操作 {@link com.insightops.agent.agent.DiagnostorAgent}。
 * </p>
 * <p>
 * <b>实现类：</b>{@link com.insightops.agent.service.impl.DiagnostorAgentServiceImpl}
 * </p>
 */
public interface DiagnostorAgentService {

    /**
     * 执行 ReAct 故障诊断。
     *
     * @param question 用户问题
     * @return 诊断结论文本
     */
    String diagnose(String question);
}
