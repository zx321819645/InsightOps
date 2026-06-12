package com.insightops.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户资料 + 角色权限（登录、/auth/me 等接口共用）
 */
@Data
@Builder
public class UserProfileVO {

    private Long userId;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    /** 用户拥有的角色列表 */
    private List<RoleVO> roles;

    /** 用户拥有的权限列表（由角色聚合去重） */
    private List<PermissionVO> permissions;
}
