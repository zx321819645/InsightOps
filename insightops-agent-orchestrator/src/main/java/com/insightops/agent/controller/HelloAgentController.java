package com.insightops.agent.controller;
import com.insightops.common.result.Result;
import com.insightops.agent.service.HelloAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author: 祝鑫
 * @CreateTime: 2026-06-10 11:44
 * @Description:
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class HelloAgentController {

    @Autowired
    private HelloAgentService helloAgentService;

    /**
     * hello-agent 最小 Demo
     * 示例：GET agent/hello?q=你好，介绍一下InsightOps
     */
    @GetMapping("/hello")
    public Result<String> hello(@RequestParam(defaultValue = "你好") String q) {
        String answer = helloAgentService.chat(q);
        return Result.success(answer);
    }

}
