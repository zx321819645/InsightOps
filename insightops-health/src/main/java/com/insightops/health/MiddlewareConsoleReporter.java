package com.insightops.health;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 应用启动完成后，在控制台直接打印各中间件连接结果。
 */
public class MiddlewareConsoleReporter implements ApplicationListener<ApplicationReadyEvent> {

    private final MiddlewareHealthProperties properties;
    private final ObjectProvider<MiddlewareChecker> checkerProvider;

    public MiddlewareConsoleReporter(MiddlewareHealthProperties properties,
                                     ObjectProvider<MiddlewareChecker> checkerProvider) {
        this.properties = properties;
        this.checkerProvider = checkerProvider;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!properties.isStartupLogEnabled()) {
            return;
        }

        List<MiddlewareChecker> checkers = checkerProvider.stream().toList();
        if (checkers.isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println("========== 中间件连接校验 ==========");
        for (MiddlewareChecker checker : checkers) {
            MiddlewareCheckResult result = checker.check();
            printResult(checker.label(), result);
        }
        System.out.println("==================================");
        System.out.println();
    }

    private void printResult(String label, MiddlewareCheckResult result) {
        if (result.success()) {
            System.out.println("[中间件校验] " + label + " 连接成功" + formatDetail(result.detail()));
            return;
        }
        System.err.println("[中间件校验] " + label + " 连接失败: " + result.detail());
    }

    private String formatDetail(String detail) {
        return StringUtils.hasText(detail) ? " (" + detail + ")" : "";
    }
}
