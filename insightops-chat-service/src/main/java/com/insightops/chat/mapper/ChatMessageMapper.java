package com.insightops.chat.mapper;

import com.insightops.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息表 {@code chat_message} 数据访问层。
 */
public interface ChatMessageMapper {

    /** 插入一条消息 */
    int insert(ChatMessage message);

    /** 查询会话下的全部消息，按创建时间正序（配合 PageHelper 分页） */
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    /** 逻辑删除指定会话下的所有消息（删除会话时级联调用） */
    int softDeleteBySessionId(@Param("sessionId") Long sessionId);
}
