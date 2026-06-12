package com.insightops.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录 / 注册成功响应
 */
@Data
@Builder
public class LoginResponse {

    // ---------- Token ----------

    private String token;

    private String tokenType;

    /** 有效期（秒） */
    private Long expiresIn;

    // ---------- 用户基本信息 ----------

    private Long userId;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    // ---------- 角色与权限 ----------

    /** 角色列表（含编码与名称） */
    private List<RoleVO> roles;

    /** 权限列表（含编码、类型、路径，供前端菜单/按钮控制） */
    private List<PermissionVO> permissions;
}
