package com.insightops.agent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 故障诊断请求体。
 * <p>
 * <b>作用：</b>封装 {@code POST /agent/diagnose} 的入参，供 Controller 接收 JSON。
 * </p>
 * <p>
 * <b>Gateway 访问示例：</b>{@code POST /agent-orchestrator/agent/diagnose}
 * <pre>{@code { "question": "order-service 响应变慢了" }}</pre>
 * </p>
 */
@Data
public class DiagnoseRequest {

    /** 用户故障描述或运维问题，必填 */
    @NotBlank(message = "question 不能为空")
    @Size(max = 2000, message = "question 最长 2000 个字符")
    private String question;
}
