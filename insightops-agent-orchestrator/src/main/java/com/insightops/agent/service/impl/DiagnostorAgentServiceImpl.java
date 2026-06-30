package com.insightops.agent.service.impl;

import com.insightops.agent.agent.DiagnostorAgent;
import com.insightops.agent.service.DiagnostorAgentService;
import com.insightops.agent.tool.OpsTools;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 诊断 Agent 业务实现（Week 3 · 任务 3.3 核心）。
 * <p>
 * 启动时通过 {@link AiServices} 构建 {@link DiagnostorAgent} 代理，
 * 绑定 {@link ChatModel}（DeepSeek）与 {@link OpsTools}（3 个 @Tool）。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DiagnostorAgentServiceImpl implements DiagnostorAgentService {

    private final ChatModel chatModel;
    private final OpsTools opsTools;

    private DiagnostorAgent agent;

    @PostConstruct
    public void init() {
        agent = AiServices.builder(DiagnostorAgent.class)
                .chatModel(chatModel)
                .tools(opsTools)
                .maxSequentialToolsInvocations(8)
                .build();
    }

    @Override
    public String diagnose(String question) {
        return agent.diagnose(question);
    }
}
