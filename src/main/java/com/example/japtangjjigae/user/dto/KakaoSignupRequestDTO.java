package com.example.japtangjjigae.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;


@Getter
public class KakaoSignupRequestDTO {

    @NotBlank(message = "소셜회원가입 위한 티켓값 필수")
    private String socialSignupTicket;

    @NotBlank(message = "이름 필수")
    private String name;

    @NotBlank(message = "핸드폰번호 필수")
    @Pattern(
        regexp = "^(01[016789])[-]?(\\d{3,4})[-]?(\\d{4})$",
        message = "핸드폰 번호 형식틀림"
    )
    private String phone;

}
