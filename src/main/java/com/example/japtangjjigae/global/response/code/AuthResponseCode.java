package com.example.japtangjjigae.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthResponseCode implements ResponseCode {

    OAUTH2_UNSUPPORTED_PROVIDER(400, "지원하지 않는 OAuth2 제공자입니다."),
    OAUTH2_INVALID_PROVIDER_ID(400, "유효하지 않은 소셜 회원 번호입니다."),
    OAUTH2_AUTHENTICATION_FAILED(401, "소셜 로그인에 실패했습니다.");

    private final int code;
    private final String message;
}
