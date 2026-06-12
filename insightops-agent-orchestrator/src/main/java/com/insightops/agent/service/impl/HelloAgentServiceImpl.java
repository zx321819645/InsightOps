package com.insightops.agent.service.impl;

import com.insightops.agent.service.HelloAgentService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: 祝鑫
 * @CreateTime: 2026-06-10 11:42
 * @Description:
 */
@Service
public class HelloAgentServiceImpl implements HelloAgentService {

    @Autowired
    private  ChatLanguageModel chatLanguageModel;
    public String chat(String question) {
        return chatLanguageModel.generate(question);
    }
}
