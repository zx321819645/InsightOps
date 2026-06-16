package com.insightops.chat.service;

import com.insightops.chat.dto.request.SendMessageRequest;
import com.insightops.chat.dto.response.MessageVO;
import com.insightops.common.result.PageResult;

/**
 * 消息业务接口。
 * <p>
 * Week 2 仅持久化消息并更新会话统计；Week 3+ 发消息后将转发至 agent-orchestrator。
 * </p>
 */
public interface MessageService {

    /** 发送消息，同步更新 session.message_count 与 last_message_at */
    MessageVO sendMessage(Long userId, Long sessionId, SendMessageRequest request);

    /** 分页查询会话历史消息，按时间正序 */
    PageResult<MessageVO> listMessages(Long userId, Long sessionId, int page, int size);
}
