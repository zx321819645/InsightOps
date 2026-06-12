package com.insightops.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: 祝鑫
 * @CreateTime: 2026-06-10 13:59
 * @Description:
 */
@Data
public class BaseEntity {
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除标识：0-未删除 1-已删除
     */
    private Integer deleted;
}
