package com.insightops.chat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话详情 / 列表项视图对象。
 */
@Data
@Builder
public class SessionVO {

    private Long id;

    private Long userId;

    private String title;

    /** 1-普通 2-告警分析 3-工单关联 */
    private Integer sessionType;

    /** 关联告警/工单 ID */
    private Long refId;

    /** 1-进行中 2-已结束 3-已归档 */
    private Integer status;

    /** 消息总数 */
    private Integer messageCount;

    /** 最后活跃时间 */
    private LocalDateTime lastMessageAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
