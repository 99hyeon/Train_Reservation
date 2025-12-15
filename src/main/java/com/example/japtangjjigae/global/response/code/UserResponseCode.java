package com.example.japtangjjigae.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserResponseCode implements ResponseCode {

    SUCCESS_LOGIN(200, "로그인에 성공"),
    SUCCESS_SIGNUP(201, "회원가입 성공"),
    SOCIAL_SIGNUP_REQUIRED(202, "최초 소셜 로그인 : 회원가입 필요"),
    USER_NOT_FOUND(404, "해당 유저 존재 안함"),
    TICKET_NOT_FOUND(404, "회원가입 티켓 못 찾음"),
    ALREADY_LIKED_SOCIAL_ACCOUNT(409, "다른 소셜 계정으로 회원가입을 함"),
    USER_DUPLICATE(409, "이미 존재하는 유저");

    private final int code;
    private final String message;
}
