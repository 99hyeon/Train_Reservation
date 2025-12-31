package com.example.japtangjjigae.token.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String token;
    private String expiration;

    public static RefreshToken createRefreshToken(Long userId, String token, String expiration){
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.userId = userId;
        newRefreshToken.token = token;
        newRefreshToken.expiration = expiration;

        return newRefreshToken;
    }

}
