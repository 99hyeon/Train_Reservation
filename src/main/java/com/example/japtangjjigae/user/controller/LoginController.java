package com.example.japtangjjigae.user.controller;

import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.jwt.TokenCookie;
import com.example.japtangjjigae.jwt.TokenTTL;
import com.example.japtangjjigae.user.dto.SignupRequestDTO;
import com.example.japtangjjigae.user.dto.SignupResponseDTO;
import com.example.japtangjjigae.user.service.LoginService;
import com.example.japtangjjigae.token.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "소셜 login API", description = "소셜 로그인 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class LoginController {

    private static final String AUTH_HEADER = "Authorization";
    private static final String COOKIE_HEADER = "Set-Cookie";

    private final LoginService loginService;
    private final TokenService tokenService;

    @Operation(
        summary = "회원가입 api",
        description = "최초로 로그인 할 시 회원가입할 api"
    )
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> signUp(@Valid @RequestBody
    SignupRequestDTO requestDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse.from(UserResponseCode.SUCCESS_SIGNUP, loginService.signUp(requestDto)));
    }

    @Operation(
        summary = "access token 발급 api",
        description = "access token, refresh token 발급 및 reissue API"
    )
    @PostMapping("/token")
    public ResponseEntity issueAccessToken(
        HttpServletRequest request, HttpServletResponse response) {
        String[] tokens = tokenService.issueAccessTokenRefreshToken(request);
        String accessToken = tokens[0];
        String refreshToken = tokens[1];
        response.setHeader(AUTH_HEADER, accessToken);
        response.addHeader(COOKIE_HEADER, TokenCookie.buildRefreshCookie(refreshToken, TokenTTL.REFRESH.seconds()));

        return ResponseEntity.status(HttpStatus.OK).body(accessToken);
    }

}
