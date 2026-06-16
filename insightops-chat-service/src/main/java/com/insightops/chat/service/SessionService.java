package com.insightops.chat.service;

import com.insightops.chat.dto.request.CreateSessionRequest;
import com.insightops.chat.dto.request.UpdateSessionRequest;
import com.insightops.chat.dto.response.SessionVO;
import com.insightops.common.result.PageResult;

/**
 * 会话业务接口。
 * <p>
 * 负责 chat_session 表的 CRUD，所有操作均校验会话归属（userId 匹配）。
 * </p>
 */
public interface SessionService {

    /** 创建新会话，默认 title=「新对话」、status=进行中 */
    SessionVO createSession(Long userId, CreateSessionRequest request);

    /** 分页查询当前用户的会话列表，按最后活跃时间倒序 */
    PageResult<SessionVO> listSessions(Long userId, int page, int size);

    /** 查询会话详情，校验归属 */
    SessionVO getSession(Long userId, Long sessionId);

    /** 更新标题或状态，至少传一个字段 */
    SessionVO updateSession(Long userId, Long sessionId, UpdateSessionRequest request);

    /** 逻辑删除会话及其下全部消息 */
    void deleteSession(Long userId, Long sessionId);
}
