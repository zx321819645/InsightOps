package com.insightops.auth.service.impl;

import com.insightops.auth.dto.request.LoginRequest;
import com.insightops.auth.dto.request.RegisterRequest;
import com.insightops.auth.dto.response.LoginResponse;
import com.insightops.auth.dto.response.PermissionVO;
import com.insightops.auth.dto.response.RoleVO;
import com.insightops.auth.dto.response.UserInfoResponse;
import com.insightops.auth.entity.SysUser;
import com.insightops.auth.entity.SysUserRole;
import com.insightops.auth.mapper.SysPermissionMapper;
import com.insightops.auth.mapper.SysRoleMapper;
import com.insightops.auth.mapper.SysUserMapper;
import com.insightops.auth.mapper.SysUserRoleMapper;
import com.insightops.auth.service.AuthService;
import com.insightops.auth.util.JwtUtil;
import com.insightops.auth.util.SnowflakeIdGenerator;
import com.insightops.common.exception.BizException;
import com.insightops.common.exception.ErrorCode;
import com.insightops.common.util.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    /** 新注册用户默认角色：开发工程师（可使用对话等功能） */
    private static final long DEFAULT_ROLE_ID = 3L;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername().trim());
        AssertUtils.notNull(user, "用户名或密码错误");
        AssertUtils.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()),
                ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        AssertUtils.isTrue(user.getStatus() != null && user.getStatus() == 1,
                ErrorCode.FORBIDDEN, "账号已被禁用");

        sysUserMapper.updateLastLogin(user.getId(), LocalDateTime.now(), clientIp);
        return buildLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (sysUserMapper.selectByUsername(username) != null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setId(SnowflakeIdGenerator.nextId());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setId(SnowflakeIdGenerator.nextId());
        userRole.setUserId(user.getId());
        userRole.setRoleId(DEFAULT_ROLE_ID);
        sysUserRoleMapper.insert(userRole);

        return buildLoginResponse(user);
    }

    @Override
    public UserInfoResponse getCurrentUser(String token) {
        Long userId = jwtUtil.parseUserId(token);
        SysUser user = sysUserMapper.selectById(userId);
        AssertUtils.notNull(user, "用户不存在或 Token 无效");
        AssertUtils.isTrue(user.getStatus() != null && user.getStatus() == 1,
                ErrorCode.FORBIDDEN, "账号已被禁用");
        return buildUserInfoResponse(user);
    }

    private LoginResponse buildLoginResponse(SysUser user) {
        List<RoleVO> roles = loadRoles(user.getId());
        List<PermissionVO> permissions = loadPermissions(user.getId());
        List<String> roleCodes = roles.stream().map(RoleVO::getRoleCode).toList();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCodes);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpireSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private UserInfoResponse buildUserInfoResponse(SysUser user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(loadRoles(user.getId()))
                .permissions(loadPermissions(user.getId()))
                .build();
    }

    private List<RoleVO> loadRoles(Long userId) {
        return sysRoleMapper.selectRolesByUserId(userId);
    }

    private List<PermissionVO> loadPermissions(Long userId) {
        return sysPermissionMapper.selectByUserId(userId);
    }
}

