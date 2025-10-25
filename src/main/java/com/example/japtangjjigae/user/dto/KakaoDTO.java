package com.example.japtangjjigae.user.dto;

import lombok.Getter;

// 카카오 응답을 담을 DTO들
public class KakaoDTO {

    @Getter
    public static class OAuthToken {
        private String token_type;
        private String access_token;
        private int expires_in;
        private String refresh_token;
        private int refresh_token_expires_in;
        private String scope;
    }

    @Getter
    public static class KakaoProfile {
        private Long id;
        private String nickname;
        private String connected_at;
    }
}
