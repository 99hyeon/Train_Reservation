package com.example.japtangjjigae.user.controller;

import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.user.dto.SignupRequestDTO;
import com.example.japtangjjigae.user.dto.signupResponseDTO;
import com.example.japtangjjigae.user.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Kakao login API", description = "카카오 로그인 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class LoginController {

    private final LoginService loginService;

    @Operation(
        summary = "회원가입 api",
        description = "최초로 로그인 할 시 회원가입할 api"
    )
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<signupResponseDTO>> signUp(@RequestBody
    SignupRequestDTO requestDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.from(UserResponseCode.SUCCESS_SIGNUP,
                loginService.signUp(requestDto)));
    }

    @Operation(
        summary = "회원가입 필요시 리다이렉트",
        description = "소셜 로그인 이후 회원가입 필요시 프론트로 리다이렉트 대신"
            + "임시로 백엔드쪽으로 티켓값만 확인 위해 만듦"
    )
    @GetMapping("/check/signup")
    public ResponseEntity<String> signUpRedirectUrl(@RequestParam("ticket")
    String ticket) {

        log.info("ticket: " + ticket);

        return ResponseEntity.ok("회원가입 티켓 확인 완료: " + ticket);
    }

}
