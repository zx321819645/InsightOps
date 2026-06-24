package com.insightops.agent.tool;

import lombok.Getter;

/**
 * Tool 执行专用错误码。
 * <p>
 * 与 {@link com.insightops.common.exception.ErrorCode} 区分：Tool 层使用 40001+ 段，
 * 便于 audit-service 与 Agent Trace 单独统计。
 * </p>
 */
@Getter
public enum ToolErrorCode {

    SUCCESS(0, "成功"),
    PARAM_INVALID(401, "参数校验失败"),
    RESOURCE_NOT_FOUND(404, "目标资源不存在"),
    INTERNAL_ERROR(501, "Tool 内部错误"),
    TIMEOUT(504, "执行超时");

    private final int code;
    private final String message;

    ToolErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
