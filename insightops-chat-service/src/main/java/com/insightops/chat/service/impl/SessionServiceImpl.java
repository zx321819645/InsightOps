package com.insightops.chat.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.insightops.chat.dto.request.CreateSessionRequest;
import com.insightops.chat.dto.request.UpdateSessionRequest;
import com.insightops.chat.dto.response.SessionVO;
import com.insightops.chat.entity.ChatSession;
import com.insightops.chat.mapper.ChatMessageMapper;
import com.insightops.chat.mapper.ChatSessionMapper;
import com.insightops.chat.service.SessionService;
import com.insightops.chat.util.SnowflakeIdGenerator;
import com.insightops.common.exception.ErrorCode;
import com.insightops.common.result.PageResult;
import com.insightops.common.util.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 会话业务实现。
 */
@Service
public class SessionServiceImpl implements SessionService {

    private static final String DEFAULT_TITLE = "新对话";

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public SessionVO createSession(Long userId, CreateSessionRequest request) {
        ChatSession session = new ChatSession();
        session.setId(SnowflakeIdGenerator.nextId());
        session.setUserId(userId);
        session.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : DEFAULT_TITLE);
        session.setSessionType(request.getSessionType() != null ? request.getSessionType() : 1);
        session.setRefId(request.getRefId());
        session.setStatus(1);
        session.setMessageCount(0);
        chatSessionMapper.insert(session);
        return toVO(session);
    }

    @Override
    public PageResult<SessionVO> listSessions(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<ChatSession> sessions = chatSessionMapper.selectByUserId(userId);
        PageInfo<ChatSession> pageInfo = new PageInfo<>(sessions);
        List<SessionVO> records = pageInfo.getList().stream().map(this::toVO).toList();
        return PageResult.of(pageInfo.getTotal(), records);
    }

    @Override
    public SessionVO getSession(Long userId, Long sessionId) {
        return toVO(requireOwnedSession(userId, sessionId));
    }

    @Override
    public SessionVO updateSession(Long userId, Long sessionId, UpdateSessionRequest request) {
        requireOwnedSession(userId, sessionId);
        AssertUtils.isTrue(StringUtils.hasText(request.getTitle()) || request.getStatus() != null,
                ErrorCode.BAD_REQUEST, "请至少提供 title 或 status");

        String title = StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : null;
        chatSessionMapper.updateTitleAndStatus(sessionId, title, request.getStatus());
        return toVO(requireOwnedSession(userId, sessionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long userId, Long sessionId) {
        requireOwnedSession(userId, sessionId);
        chatMessageMapper.softDeleteBySessionId(sessionId);
        chatSessionMapper.softDelete(sessionId);
    }

    /**
     * 校验会话存在且属于当前用户，防止越权访问。
     */
    private ChatSession requireOwnedSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        AssertUtils.notNull(session, "会话不存在");
        AssertUtils.isTrue(userId.equals(session.getUserId()), ErrorCode.FORBIDDEN, "无权访问该会话");
        return session;
    }

    private SessionVO toVO(ChatSession session) {
        return SessionVO.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .title(session.getTitle())
                .sessionType(session.getSessionType())
                .refId(session.getRefId())
                .status(session.getStatus())
                .messageCount(session.getMessageCount())
                .lastMessageAt(session.getLastMessageAt())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
