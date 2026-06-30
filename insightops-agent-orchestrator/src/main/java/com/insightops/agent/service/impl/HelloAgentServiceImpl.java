package com.insightops.agent.service.impl;

import com.insightops.agent.service.HelloAgentService;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * hello-agent 最小 Demo：纯 LLM 对话，不绑定 Tool。
 */
@Service
public class HelloAgentServiceImpl implements HelloAgentService {

    @Autowired
    private ChatModel chatModel;

    @Override
    public String chat(String question) {
        return chatModel.chat(question);
    }
}
