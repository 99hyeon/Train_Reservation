package com.example.japtangjjigae.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    private Long id;

    private String nickname;
    private String profileImgUrl;
    private Long kakaoId;

}
