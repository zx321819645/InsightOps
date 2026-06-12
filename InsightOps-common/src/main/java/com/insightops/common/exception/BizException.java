package com.insightops.common.exception;

import lombok.Getter;

/**
 * 业务异常。
 * <p>
 * 在 Service 层主动抛出，表示可预期的业务失败（如「用户不存在」「工单已关闭」）。
 * 由 {@link GlobalExceptionHandler} 捕获后转换为统一的 {@link com.insightops.common.result.Result} 返回。
 * </p>
 *
 * <pre>
 * 使用示例：throw new BizException(ErrorCode.NOT_FOUND, "会话不存在");
 * </pre>
 */
@Getter
public class BizException extends RuntimeException {

    /** 错误码，与 {@link ErrorCode} 中的 code 对应 */
    private final int code;

    /**
     * 使用错误码的默认提示信息
     *
     * @param errorCode 预定义错误码
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码 + 自定义提示（更具体的业务描述）
     *
     * @param errorCode 预定义错误码
     * @param message   自定义错误描述
     */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 完全自定义错误码和提示（特殊场景使用，优先用 ErrorCode 枚举）
     *
     * @param code    数字错误码
     * @param message 错误描述
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
