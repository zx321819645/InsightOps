package com.insightops.auth.entity;

import com.insightops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: 祝鑫
 * @CreateTime: 2026-06-10 14:02
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {
    /**
     * 主键
     */
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0-禁用 1-正常
     */
    private Integer status;
}
