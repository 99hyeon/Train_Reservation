package com.example.japtangjjigae.user.controller;

import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.service.KakaoLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Kakao login API", description = "카카오 로그인 API")
@RequiredArgsConstructor
@RestController
@RequestMapping
public class KaKaoLoginController {

    private final KakaoLoginService kakaoLoginService;

    @Operation(
        summary = "카카오 로그인 url",
        description = "카카오 로그인 할 url 받는 api"
    )
    @GetMapping("/kakao/login")
    public ApiResponse<String> getKakaoLoginUrl() {
        String url = kakaoLoginService.getKakaoLoginUrl();

        return ApiResponse.of("200", "URL_GET_SUCCESS", url);
    }

    @Operation(
        summary = "카카오 로그인 콜백 엔드포인트",
        description = "카카오 로그인 및 동의 후 카카오 인증 서버에서 리다이렉트하는 API"
    )
    @GetMapping("/auth/login/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code, HttpServletResponse httpServletResponse) {
        User user = kakaoLoginService.kakaoLogin(code, httpServletResponse);

        return ResponseEntity.ok("ok");
    }

}
