package com.example.japtangjjigae.user.service;

import com.example.japtangjjigae.user.dto.KakaoDTO;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.repository.UserRepository;
import com.example.japtangjjigae.util.KakaoUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;

    public String getKakaoLoginUrl() {
        String clientId = kakaoUtil.getClient();
        String redirect = kakaoUtil.getRedirect();
        String loginUrl =
            "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId + "&redirect_uri="
                + redirect + "&response_type=code";

        return loginUrl;
    }

    public User kakaoLogin(String code, HttpServletResponse httpServletResponse) {
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(code);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        log.info(kakaoProfile.getId().toString());

//        String token = jwtUtil.createAccessToken(user.getEmail(), user.getRole().toString());
//        httpServletResponse.setHeader("Authorization", token);

//        return user;

        return new User();
    }

}
