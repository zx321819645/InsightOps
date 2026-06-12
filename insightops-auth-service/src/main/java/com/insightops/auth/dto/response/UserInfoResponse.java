package com.insightops.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 当前登录用户信息（GET /auth/auth/me）
 * <p>
 * 不含 Token，用于前端刷新页面后恢复用户身份与权限。
 * 字段与 {@link UserProfileVO} 一致，后续可在 Service 层复用组装逻辑。
 * </p>
 */
@Data
@Builder
public class UserInfoResponse {

    private Long userId;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    private List<RoleVO> roles;

    private List<PermissionVO> permissions;
}
