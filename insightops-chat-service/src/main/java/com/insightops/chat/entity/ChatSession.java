package com.insightops.chat.entity;

import com.insightops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会话实体，对应表 {@code chat_session}。
 * <p>
 * 一次完整对话的容器，类似 ChatGPT 左侧的一个对话窗口。
 * 与 {@link ChatMessage} 为一对多关系：一个会话包含多条消息。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSession extends BaseEntity {

    /** 主键（雪花 ID） */
    private Long id;

    /** 所属用户 ID，关联 sys_user.id，用于数据隔离 */
    private Long userId;

    /** 会话标题，列表页展示，默认「新对话」 */
    private String title;

    /** 会话类型：1-普通对话 2-告警分析 3-工单关联 */
    private Integer sessionType;

    /** 关联业务 ID，配合 sessionType 使用（如 alert_record.id、ticket.id） */
    private Long refId;

    /** 会话状态：1-进行中 2-已结束 3-已归档 */
    private Integer status;

    /** 消息总数（冗余字段，避免列表页频繁 COUNT） */
    private Integer messageCount;

    /** 最后一条消息时间，用于会话列表按活跃度排序 */
    private LocalDateTime lastMessageAt;
}
