package com.insightops.auth.controller;

import com.insightops.auth.dto.request.LoginRequest;
import com.insightops.auth.dto.request.RegisterRequest;
import com.insightops.auth.dto.response.LoginResponse;
import com.insightops.auth.dto.response.UserInfoResponse;
import com.insightops.auth.service.AuthService;
import com.insightops.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：/auth/auth/login、/auth/auth/register、/auth/auth/me
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        return Result.success(authService.login(request, clientIp));
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> me(@RequestHeader("token") String token) {
        return Result.success(authService.getCurrentUser(token));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
