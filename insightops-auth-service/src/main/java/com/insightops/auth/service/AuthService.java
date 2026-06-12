package com.insightops.auth.service;

import com.insightops.auth.dto.request.LoginRequest;
import com.insightops.auth.dto.request.RegisterRequest;
import com.insightops.auth.dto.response.LoginResponse;
import com.insightops.auth.dto.response.UserInfoResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, String clientIp);

    LoginResponse register(RegisterRequest request);

    UserInfoResponse getCurrentUser(String token);
}
