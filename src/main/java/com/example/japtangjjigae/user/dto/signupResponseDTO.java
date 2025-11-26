package com.example.japtangjjigae.user.dto;

import com.example.japtangjjigae.user.common.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class signupResponseDTO {

    private Long userId;
    private OAuthProvider oAuthProvider;

}
