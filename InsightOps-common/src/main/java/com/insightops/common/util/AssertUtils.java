package com.insightops.common.util;

import com.insightops.common.exception.BizException;
import com.insightops.common.exception.ErrorCode;

/**
 * 断言工具类。
 * <p>
 * 在 Service 层做前置条件校验：条件不满足时直接抛出 {@link BizException}，
 * 减少重复的 if-throw 样板代码，异常最终由 {@link com.insightops.common.exception.GlobalExceptionHandler} 统一处理。
 * </p>
 *
 * <pre>
 * 使用示例：
 *   AssertUtils.notNull(user, "用户不存在");
 *   AssertUtils.isTrue(order.getStatus() == 1, ErrorCode.BIZ_ERROR, "订单状态不允许操作");
 * </pre>
 */
public final class AssertUtils {

    private AssertUtils() {
    }

    /**
     * 断言对象不为 null
     *
     * @param obj     待检查对象
     * @param message 为 null 时的错误提示
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 断言字符串非 null 且非空白（trim 后不为空）
     *
     * @param str     待检查字符串
     * @param message 校验失败时的错误提示
     */
    public static void notBlank(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 断言表达式为 true，否则抛出对应错误码的默认提示
     *
     * @param expression 条件表达式
     * @param errorCode  条件为 false 时使用的错误码
     */
    public static void isTrue(boolean expression, ErrorCode errorCode) {
        if (!expression) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言表达式为 true，否则抛出自定义提示的业务异常
     *
     * @param expression 条件表达式
     * @param errorCode  错误码
     * @param message    条件为 false 时的自定义错误描述
     */
    public static void isTrue(boolean expression, ErrorCode errorCode, String message) {
        if (!expression) {
            throw new BizException(errorCode, message);
        }
    }
}
