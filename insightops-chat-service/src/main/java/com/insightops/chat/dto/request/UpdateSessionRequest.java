package com.insightops.chat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新会话请求体。
 * <p>
 * title 与 status 至少传一个；只传需要修改的字段即可。
 * </p>
 */
@Data
public class UpdateSessionRequest {

    /** 新标题 */
    @Size(max = 256, message = "会话标题最长 256 个字符")
    private String title;

    /** 新状态：1-进行中 2-已结束 3-已归档 */
    @Min(value = 1, message = "会话状态无效")
    @Max(value = 3, message = "会话状态无效")
    private Integer status;
}
