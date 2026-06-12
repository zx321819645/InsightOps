package com.insightops.common.exception;

import lombok.Getter;

/**
 * 全局业务错误码枚举。
 * <p>
 * 约定：0 为成功；4xxxx 为客户端错误；5xxxx 为服务端 / 业务错误。
 * 各微服务抛 {@link BizException} 或返回 {@link com.insightops.common.result.Result} 时统一引用此枚举。
 * </p>
 */
@Getter
public enum ErrorCode {

    /** 操作成功 */
    SUCCESS(0, "操作成功"),

    /** 请求参数缺失、格式错误或校验不通过 */
    BAD_REQUEST(400, "请求参数错误"),

    /** JWT 无效、未登录、Token 过期 */
    UNAUTHORIZED(401, "未登录或 Token 无效"),

    /** 已登录但无权限访问该资源 */
    FORBIDDEN(403, "无访问权限"),

    /** 请求的资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 业务规则校验失败（如用户名已存在、状态不允许操作） */
    BIZ_ERROR(500, "业务处理失败"),

    /** 未预期的系统异常（数据库异常、空指针等） */
    SYSTEM_ERROR(50001, "系统内部错误");

    /** 数字错误码，返回给前端 */
    private final int code;

    /** 默认错误描述，可被自定义 message 覆盖 */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
