package com.insightops.chat.util;

import com.insightops.common.exception.BizException;
import com.insightops.common.exception.ErrorCode;
import com.insightops.common.util.AssertUtils;

/**
 * 当前登录用户 ID 解析工具。
 * <p>
 * Gateway {@code JwtAuthGlobalFilter} 校验 Token 后，将 JWT 中的 userId 写入 {@code X-User-Id} 请求头，
 * 下游微服务统一通过本工具获取，避免各服务重复解析 JWT。
 * </p>
 */
public final class UserIdResolver {

    private UserIdResolver() {
    }

    /**
     * 从 Gateway 传入的请求头解析用户 ID。
     *
     * @param userIdHeader {@code X-User-Id} 请求头值
     * @return 用户 ID
     */
    public static Long resolve(String userIdHeader) {
        AssertUtils.notBlank(userIdHeader, "未登录或 Token 缺失");
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException ex) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户标识无效");
        }
    }
}
