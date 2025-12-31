package com.example.japtangjjigae.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final JWTUtil jwtUtil;

    public Long currentUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String accessToken = request.getHeader("Authorization").substring(7);

        if (jwtUtil.isExpired(accessToken)) {
            throw new IllegalArgumentException("Token expired");
        }

        if (jwtUtil.getCategory(accessToken) != TokenCategory.ACCESS) {
            throw new IllegalArgumentException("Not access token");
        }

        return jwtUtil.getUserId(accessToken);
    }

}
