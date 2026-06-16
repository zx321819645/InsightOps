package com.insightops.chat.entity;

import com.insightops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体，对应表 {@code chat_message}。
 * <p>
 * 会话中的每一轮发言，遵循 LLM 标准角色模型（user / assistant / system / tool）。
 * Week 3+ Agent 接入后，assistant 与 tool 类型的消息由 orchestrator 写入。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends BaseEntity {

    /** 主键（雪花 ID） */
    private Long id;

    /** 所属会话 ID，关联 chat_session.id */
    private Long sessionId;

    /** 发送者 ID；用户消息为实际 userId，系统/Agent 消息为 0 */
    private Long userId;

    /** 消息角色：user / assistant / system / tool */
    private String role;

    /** 消息正文（用户输入、Agent 回复或 Tool 返回） */
    private String content;

    /** 内容格式：1-纯文本 2-Markdown 3-JSON */
    private Integer contentType;

    /** 关联 Agent 运行实例 ID（agent_run.id），Week 3+ 使用 */
    private Long agentRunId;

    /** Token 消耗，用于成本统计与限流（Week 11 可观测性） */
    private Integer tokenCount;

    /** JSON 扩展信息，如 Tool 名称、入参/出参摘要，供 Trace 可视化 */
    private String metadata;
}
