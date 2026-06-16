package com.insightops.chat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建会话请求体。
 * <p>
 * 所有字段均可选；不传 title 时默认「新对话」，不传 sessionType 时默认 1（普通对话）。
 * </p>
 */
@Data
public class CreateSessionRequest {

    /** 会话标题，最长 256 字符 */
    @Size(max = 256, message = "会话标题最长 256 个字符")
    private String title;

    /** 会话类型：1-普通 2-告警分析 3-工单关联 */
    @Min(value = 1, message = "会话类型无效")
    @Max(value = 3, message = "会话类型无效")
    private Integer sessionType;

    /** 关联业务 ID（告警 ID / 工单 ID 等） */
    private Long refId;
}
