package com.example.japtangjjigae.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenResponseCode implements ResponseCode {

    REFRESH_TOKEN_EXPIRED(404, "refresh token 만료됨"),
    REFRESH_TOKEN_NOT_FOUND(404, "refresh token 없음");

    private final int code;
    private final String message;
}
