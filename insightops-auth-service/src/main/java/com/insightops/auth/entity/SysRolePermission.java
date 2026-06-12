package com.insightops.auth.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: 祝鑫
 * @CreateTime: 2026-06-10 15:21
 * @Description:
 */
@Data
public class SysRolePermission {
    /**
     * 主键
     */
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
