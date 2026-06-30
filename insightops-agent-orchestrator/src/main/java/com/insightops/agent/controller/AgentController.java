package com.insightops.agent.controller;

import com.insightops.agent.dto.request.DiagnoseRequest;
import com.insightops.agent.service.DiagnostorAgentService;
import com.insightops.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 诊断 HTTP 入口（ReAct + Tool 正式接口）。
 * <p>
 * <b>作用：</b>对外提供故障诊断 API，调用 {@link DiagnostorAgentService} 触发 ReAct 流程。
 * 与 {@link HelloAgentController} 区分：hello 是纯 LLM 对话，本 Controller 是带 Tool 的诊断。
 * </p>
 * <p>
 * <b>你需要完成：</b>
 * <ul>
 *   <li>注入 {@link DiagnostorAgentService}</li>
 *   <li>实现 {@link #diagnose(DiagnoseRequest)}，调用 service 并返回 {@link Result#success(String)}</li>
 * </ul>
 * </p>
 * <p>
 * <b>验收：</b>POST question=「order-service 响应变慢」→ Agent 依次调 3 个 Tool 并返回诊断结论。
 * </p>
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final DiagnostorAgentService diagnostorAgentService;

    /**
     * ReAct 故障诊断。
     * <p>
     * 示例：{@code POST /agent-orchestrator/agent/diagnose}
     * </p>
     */
    @PostMapping("/diagnose")
    public Result<String> diagnose(@Valid @RequestBody DiagnoseRequest request) {
//        throw new UnsupportedOperationException("待实现：调用 DiagnostorAgentService.diagnose");
        String answer = diagnostorAgentService.diagnose(request.getQuestion());
        return Result.success(answer);
    }
}
