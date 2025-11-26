package com.example.japtangjjigae.user.dto;

import com.example.japtangjjigae.user.common.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private OAuthProvider oAuthProvider;

}
