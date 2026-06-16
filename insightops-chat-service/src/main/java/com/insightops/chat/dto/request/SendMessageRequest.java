package com.insightops.chat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送消息请求体。
 * <p>
 * Week 2 前端一般只发 role=user 的消息；role 不传时默认为 user。
 * Week 3+ Agent 回复由 orchestrator 直接写库，不走此接口。
 * </p>
 */
@Data
public class SendMessageRequest {

    /** 消息正文 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 65535, message = "消息内容过长")
    private String content;

    /** 消息角色，默认 user */
    @Pattern(regexp = "user|assistant|system|tool", message = "消息角色无效")
    private String role;

    /** 内容格式：1-文本 2-Markdown 3-JSON，默认 1 */
    @Min(value = 1, message = "内容类型无效")
    @Max(value = 3, message = "内容类型无效")
    private Integer contentType;
}
