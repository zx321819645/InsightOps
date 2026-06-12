package com.insightops.agent;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Agent 编排服务启动类
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.insightops")
public class AgentOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentOrchestratorApplication.class, args);
    }
}
