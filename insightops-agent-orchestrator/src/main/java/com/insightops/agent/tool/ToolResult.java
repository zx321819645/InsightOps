package com.insightops.agent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tool 执行统一出参。
 * <p>
 * LangChain4j ReAct 循环中，{@link #toObservation()} 的返回值作为 Observation 注入下一轮 Prompt。
 * Week 4 起 audit-service 可序列化完整 JSON 做审计。
 * </p>
 *
 * 详见 docs/tool-spi.md §6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int OBSERVATION_MAX_LENGTH = 2048;

    /** 是否执行成功 */
    private boolean success;

    /** 业务错误码，成功时为 0 */
    private int errorCode;

    /** 错误描述，成功时为 null */
    private String errorMessage;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /** Tool 名称，便于 Trace */
    private String toolName;

    /** 业务载荷，结构因 Tool 而异 */
    private Object data;

    /** 给 LLM 阅读的摘要（必填，即使 success=false 也要有） */
    private String summary;

    public static ToolResult success(String toolName, String summary, Object data, long durationMs) {
        return ToolResult.builder()
                .success(true)
                .errorCode(ToolErrorCode.SUCCESS.getCode())
                .durationMs(durationMs)
                .toolName(toolName)
                .data(data)
                .summary(summary)
                .build();
    }

    public static ToolResult fail(String toolName, ToolErrorCode errorCode, String errorMessage,
                                  String summary, long durationMs) {
        return ToolResult.builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .toolName(toolName)
                .summary(summary)
                .build();
    }

    public static ToolResult validationError(String toolName, String errorMessage, long durationMs) {
        return fail(toolName, ToolErrorCode.PARAM_INVALID, errorMessage,
                "参数校验失败：" + errorMessage, durationMs);
    }

    public static ToolResult notFound(String toolName, String errorMessage, long durationMs) {
        return fail(toolName, ToolErrorCode.RESOURCE_NOT_FOUND, errorMessage,
                "资源不存在：" + errorMessage, durationMs);
    }

    public static ToolResult internalError(String toolName, String errorMessage, long durationMs) {
        return fail(toolName, ToolErrorCode.INTERNAL_ERROR, errorMessage,
                "Tool 执行失败：" + errorMessage, durationMs);
    }

    public static ToolResult timeout(String toolName, long durationMs) {
        return fail(toolName, ToolErrorCode.TIMEOUT, ToolErrorCode.TIMEOUT.getMessage(),
                "执行超时，请缩小查询范围或稍后重试", durationMs);
    }

    /**
     * 转为 LangChain4j Observation 文本：summary + 精简 data JSON。
     * 失败时仅返回 summary，避免 LLM 被冗余字段干扰。
     */
    public String toObservation() {
        if (!success || data == null) {
            return summary;
        }
        try {
            String dataJson = OBJECT_MAPPER.writeValueAsString(data);
            String observation = summary + "\n" + dataJson;
            if (observation.length() <= OBSERVATION_MAX_LENGTH) {
                return observation;
            }
            int dataBudget = Math.max(0, OBSERVATION_MAX_LENGTH - summary.length() - 6);
            return summary + "\n" + dataJson.substring(0, Math.min(dataJson.length(), dataBudget)) + "...";
        } catch (JsonProcessingException e) {
            return summary;
        }
    }

    @Override
    public String toString() {
        return toObservation();
    }
}
