package com.example.japtangjjigae.jwt;

import org.springframework.http.ResponseCookie;

public final class TokenCookie {

    public static final String REFRESH_NAME = "REFRESH_TOKEN";

    public static String buildRefreshCookie(String refreshToken, long ttlSeconds) {
        return ResponseCookie.from(REFRESH_NAME, refreshToken)
            .httpOnly(true)
//            .secure(true) //HTTPS 배포시엔 true로 설정 변경 필요
//            .sameSite("None")
            .path("/")
            .maxAge(ttlSeconds)
            .build()
            .toString();
    }

}
