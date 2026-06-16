package com.insightops.chat.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.insightops.chat.dto.request.SendMessageRequest;
import com.insightops.chat.dto.response.MessageVO;
import com.insightops.chat.entity.ChatMessage;
import com.insightops.chat.entity.ChatSession;
import com.insightops.chat.mapper.ChatMessageMapper;
import com.insightops.chat.mapper.ChatSessionMapper;
import com.insightops.chat.service.MessageService;
import com.insightops.chat.util.SnowflakeIdGenerator;
import com.insightops.common.exception.ErrorCode;
import com.insightops.common.result.PageResult;
import com.insightops.common.util.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息业务实现。
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Long userId, Long sessionId, SendMessageRequest request) {
        ChatSession session = requireOwnedSession(userId, sessionId);

        ChatMessage message = new ChatMessage();
        message.setId(SnowflakeIdGenerator.nextId());
        message.setSessionId(sessionId);
        // 用户消息记录真实 userId；系统/Agent 消息统一写 0
        message.setUserId("user".equals(resolveRole(request)) ? userId : 0L);
        message.setRole(resolveRole(request));
        message.setContent(request.getContent().trim());
        message.setContentType(request.getContentType() != null ? request.getContentType() : 1);
        message.setTokenCount(0);
        chatMessageMapper.insert(message);

        // 冗余更新 session 统计字段，供列表页展示与排序
        int messageCount = session.getMessageCount() != null ? session.getMessageCount() + 1 : 1;
        LocalDateTime now = LocalDateTime.now();
        chatSessionMapper.updateMessageStats(sessionId, messageCount, now);

        message.setCreatedAt(now);
        return toVO(message);
    }

    @Override
    public PageResult<MessageVO> listMessages(Long userId, Long sessionId, int page, int size) {
        requireOwnedSession(userId, sessionId);
        PageHelper.startPage(page, size);
        List<ChatMessage> messages = chatMessageMapper.selectBySessionId(sessionId);
        PageInfo<ChatMessage> pageInfo = new PageInfo<>(messages);
        List<MessageVO> records = pageInfo.getList().stream().map(this::toVO).toList();
        return PageResult.of(pageInfo.getTotal(), records);
    }

    /** 解析消息角色，前端不传时默认为 user */
    private String resolveRole(SendMessageRequest request) {
        return StringUtils.hasText(request.getRole()) ? request.getRole().trim() : "user";
    }

    /** 校验会话存在且属于当前用户 */
    private ChatSession requireOwnedSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        AssertUtils.notNull(session, "会话不存在");
        AssertUtils.isTrue(userId.equals(session.getUserId()), ErrorCode.FORBIDDEN, "无权访问该会话");
        return session;
    }

    private MessageVO toVO(ChatMessage message) {
        return MessageVO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .userId(message.getUserId())
                .role(message.getRole())
                .content(message.getContent())
                .contentType(message.getContentType())
                .agentRunId(message.getAgentRunId())
                .tokenCount(message.getTokenCount())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
