package com.example.japtangjjigae.user.dto;

import com.example.japtangjjigae.user.common.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;


@Getter
public class SignupRequestDTO {

    private String socialSignupTicket;

    @NotBlank(message = "이름 필수")
    private String name;

    @NotBlank(message = "핸드폰번호 필수")
    @Pattern(
        regexp = "^(01[016789])[-]?(\\d{3,4})[-]?(\\d{4})$",
        message = "핸드폰 번호 형식틀림"
    )
    private String phone;

    @NotNull(message = "소셜로그인 제공자 필수")
    private OAuthProvider oauthProvider;

}
