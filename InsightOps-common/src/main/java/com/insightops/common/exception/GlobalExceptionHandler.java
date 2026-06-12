package com.insightops.common.exception;

import com.insightops.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * <p>
 * 拦截 Controller 层抛出的各类异常，统一包装为 {@link Result} 格式返回，
 * 避免直接暴露堆栈信息，同时保证前端收到的响应结构一致。
 * </p>
 * <p>
 * 引入 common 模块的微服务需开启组件扫描（{@code @SpringBootApplication} 默认扫描当前包及子包），
 * 或将此类所在包 {@code com.insightops.common} 加入扫描路径。
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理主动抛出的业务异常（{@link BizException}）
     * 返回业务错误码和具体提示，日志级别为 warn
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return new Result<>(e.getCode(), e.getMessage(), null);
    }

    /**
     * 处理参数校验失败（{@code @Valid}、{@code @Validated} 注解触发）
     * 如：必填字段为空、格式不正确等
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception e) {
        log.warn("参数校验失败", e);
        return Result.error(ErrorCode.BAD_REQUEST, "请求参数错误");
    }

    /**
     * 兜底处理所有未捕获的异常（空指针、数据库异常等）
     * 对外只返回「系统内部错误」，详细堆栈写入 error 日志
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR);
    }
}
