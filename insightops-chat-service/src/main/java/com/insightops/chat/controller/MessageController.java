package com.insightops.chat.controller;

import com.insightops.chat.dto.request.SendMessageRequest;
import com.insightops.chat.dto.response.MessageVO;
import com.insightops.chat.service.MessageService;
import com.insightops.chat.util.UserIdResolver;
import com.insightops.common.result.PageResult;
import com.insightops.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息接口。
 * <p>
 * Gateway 路径前缀 {@code /chat/sessions/{sessionId}/messages}，各方法路径与方法名一致。
 * Week 2 发消息仅写库；Week 3+ 将在此接口中异步调用 agent-orchestrator 生成 AI 回复。
 * </p>
 */
@RestController
@RequestMapping("/sessions/{sessionId}/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /** 在指定会话中发送消息 */
    @PostMapping("/sendMessage")
    public Result<MessageVO> sendMessage(@RequestHeader("X-User-Id") String userIdHeader,
                                         @PathVariable Long sessionId,
                                         @Valid @RequestBody SendMessageRequest request) {
        Long userId = UserIdResolver.resolve(userIdHeader);
        return Result.success(messageService.sendMessage(userId, sessionId, request));
    }

    /** 分页查询会话历史消息，按时间正序（适合聊天窗口从上到下展示） */
    @GetMapping("/listMessages")
    public Result<PageResult<MessageVO>> listMessages(@RequestHeader("X-User-Id") String userIdHeader,
                                                      @PathVariable Long sessionId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "50") int size) {
        Long userId = UserIdResolver.resolve(userIdHeader);
        return Result.success(messageService.listMessages(userId, sessionId, page, size));
    }
}
