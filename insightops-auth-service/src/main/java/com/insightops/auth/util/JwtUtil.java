package com.insightops.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.insightops.auth.config.JwtProperties;
import com.insightops.common.exception.BizException;
import com.insightops.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * JWT 签发工具
 */
@Component
public class JwtUtil {

    @Autowired
    private JwtProperties jwtProperties;

    public String generateToken(Long userId, String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireHours(), ChronoUnit.HOURS);
        return JWT.create()
                .withClaim("userId", String.valueOf(userId))
                .withClaim("username", username)
                .withClaim("roles", roles)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expireAt))
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
    }

    public long getExpireSeconds() {
        return jwtProperties.getExpireHours() * 3600L;
    }

    public Long parseUserId(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
            String userId = decodedJWT.getClaim("userId").asString();
            if (userId == null || userId.isBlank()) {
                throw new BizException(ErrorCode.UNAUTHORIZED, "Token 无效");
            }
            return Long.parseLong(userId);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Token 无效或已过期");
        }
    }
}
