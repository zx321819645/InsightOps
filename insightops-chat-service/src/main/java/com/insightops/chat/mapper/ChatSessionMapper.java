package com.insightops.chat.mapper;

import com.insightops.chat.entity.ChatSession;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话表 {@code chat_session} 数据访问层。
 */
public interface ChatSessionMapper {

    /** 创建会话 */
    int insert(ChatSession session);

    /** 按 ID 查询未删除的会话 */
    ChatSession selectById(@Param("id") Long id);

    /** 查询用户的全部会话，按最后消息时间倒序（配合 PageHelper 分页） */
    List<ChatSession> selectByUserId(@Param("userId") Long userId);

    /** 更新标题和/或状态，null 字段不更新 */
    int updateTitleAndStatus(@Param("id") Long id,
                             @Param("title") String title,
                             @Param("status") Integer status);

    /** 发消息后同步更新消息计数与最后活跃时间 */
    int updateMessageStats(@Param("id") Long id,
                           @Param("messageCount") Integer messageCount,
                           @Param("lastMessageAt") LocalDateTime lastMessageAt);

    /** 逻辑删除会话 */
    int softDelete(@Param("id") Long id);
}
