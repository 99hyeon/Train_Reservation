package com.example.japtangjjigae.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialLoginResponseCode implements ResponseCode {

    KAKAO_URL_URL_ISSUED(200, "카카오 로그인 URL을 성공적으로 발급했습니다.");

    private final int code;
    private final String message;

}
