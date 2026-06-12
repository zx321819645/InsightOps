package com.insightops.common.result;

import com.insightops.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应体。
 * <p>
 * 所有微服务对外接口统一返回此结构，前端 / 调用方只需解析 code、message、data 三个字段。
 * </p>
 *
 * <pre>
 * 成功示例：{ "code": 0, "message": "操作成功", "data": { ... } }
 * 失败示例：{ "code": 50000, "message": "业务处理失败", "data": null }
 * </pre>
 *
 * @param <T> data 字段的业务数据类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    /** 状态码：0 表示成功，非 0 表示失败（详见 {@link ErrorCode}） */
    private Integer code;

    /** 提示信息：成功或失败时返回给前端的描述 */
    private String message;

    /** 业务数据：成功时返回；失败时一般为 null */
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 返回给前端的业务数据
     */
    public static <E> Result<E> success(E data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（无数据，如删除、更新操作）
     */
    public static Result<Void> success() {
        return success(null);
    }

    /**
     * 失败响应（自定义错误信息，使用默认业务错误码）
     *
     * @param message 错误描述
     */
    public static Result<Void> error(String message) {
        return new Result<>(ErrorCode.BIZ_ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（使用预定义错误码及其默认提示）
     *
     * @param errorCode 错误码枚举
     */
    public static Result<Void> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败响应（使用预定义错误码 + 自定义提示，覆盖默认 message）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误描述
     */
    public static Result<Void> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }
}
