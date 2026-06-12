package com.insightops.auth.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 权限信息（返回给前端做菜单、按钮、接口鉴权）
 */
@Data
@Builder
public class PermissionVO {

    /** 权限编码，如 chat:use、ticket:approve */
    private String permCode;

    /** 权限名称，如 使用对话、审批工单 */
    private String permName;

    /**
     * 权限类型：1-菜单 2-按钮 3-API
     */
    private Integer permType;

    /** 接口路径模式，如 /chat/**（Gateway 实际路由） */
    private String path;

    /** HTTP 方法，null 表示不限制 */
    private String method;
}
