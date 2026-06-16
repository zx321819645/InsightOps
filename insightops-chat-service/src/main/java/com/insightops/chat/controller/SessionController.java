package com.insightops.chat.controller;

import com.insightops.chat.dto.request.CreateSessionRequest;
import com.insightops.chat.dto.request.UpdateSessionRequest;
import com.insightops.chat.dto.response.SessionVO;
import com.insightops.chat.service.SessionService;
import com.insightops.chat.util.UserIdResolver;
import com.insightops.common.result.PageResult;
import com.insightops.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会话管理接口。
 * <p>
 * Gateway 路径前缀 {@code /chat/sessions}，各方法路径与方法名一致。
 * 用户身份由 Gateway JWT 过滤器解析后通过 {@code X-User-Id} 请求头传入。
 * </p>
 */
@RestController
@RequestMapping("/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /** 创建会话，body 可省略（默认「新对话」） */
    @PostMapping("/createSession")
    public Result<SessionVO> createSession(@RequestHeader("X-User-Id") String userIdHeader,
                                           @Valid @RequestBody(required = false) CreateSessionRequest request) {
        Long userId = UserIdResolver.resolve(userIdHeader);
        CreateSessionRequest body = request != null ? request : new CreateSessionRequest();
        return Result.success(sessionService.createSession(userId, body));
    }

    /** 当前用户的会话列表，按最后活跃时间倒序 */
    @GetMapping("/listSessions")
    public Result<PageResult<SessionVO>> listSessions(@RequestHeader("X-User-Id") String userIdHeader,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        Long userId = UserIdResolver.resolve(userIdHeader);
        return Result.success(sessionService.listSessions(userId, page, size));
    }

    /** 会话详情 */
    @GetMapping("/getSession/{sessionId}")
    public Result<SessionVO> getSession(@RequestHeader("X-User-Id") String userIdHeader,
                                        @PathVariable Long sessionId) {
        Long userId = UserIdResolver.resolve(userIdHeader);
        return Result.success(sessionService.getSession(userId, sessionId));
    }

    /** 重命名或更新状态（归档等） */
    @PutMapping("/updateSession/{sessionId}")
    public Result<SessionVO> updateSession(@RequestHeader("X-User-Id") String userIdHeader,
                                           @PathVariable Long sessionId,
                                           @Valid @RequestBody UpdateSessionRequest request) {
        Long userId = UserIdResolver.resolve(userIdHeader);
        return Result.success(sessionService.updateSession(userId, sessionId, request));
    }

    /** 删除会话（含其下全部消息） */
    @DeleteMapping("/deleteSession/{sessionId}")
    public Result<Void> deleteSession(@RequestHeader("X-User-Id") String userIdHeader,
                                      @PathVariable Long sessionId) {
        Long userId = UserIdResolver.resolve(userIdHeader);
        sessionService.deleteSession(userId, sessionId);
        return Result.success();
    }
}
