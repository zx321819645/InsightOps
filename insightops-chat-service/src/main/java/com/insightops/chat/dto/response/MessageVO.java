package com.insightops.chat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息详情 / 列表项视图对象。
 */
@Data
@Builder
public class MessageVO {

    private Long id;

    private Long sessionId;

    /** 发送者 ID，0 表示系统/Agent */
    private Long userId;

    /** user / assistant / system / tool */
    private String role;

    private String content;

    /** 1-文本 2-Markdown 3-JSON */
    private Integer contentType;

    /** 关联 Agent 运行 ID */
    private Long agentRunId;

    /** Token 消耗 */
    private Integer tokenCount;

    /** Tool 调用摘要等 JSON 扩展 */
    private String metadata;

    private LocalDateTime createdAt;
}
