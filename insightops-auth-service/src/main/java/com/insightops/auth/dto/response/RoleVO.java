package com.insightops.auth.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 角色信息（返回给前端展示与鉴权）
 */
@Data
@Builder
public class RoleVO {

    /** 角色编码，如 ADMIN、OPS、DEV */
    private String roleCode;

    /** 角色名称，如 管理员、运维工程师 */
    private String roleName;
}
