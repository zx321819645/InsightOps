package com.insightops.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 会话管理服务启动类。
 * <p>
 * 职责：管理 chat_session / chat_message，提供对话历史的 CRUD。
 * 扫描 {@code com.insightops} 以加载 common 模块的全局异常处理器。
 * </p>
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.insightops")
@MapperScan("com.insightops.chat.mapper")
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
