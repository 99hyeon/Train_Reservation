package com.example.japtangjjigae.user.entity;

import com.example.japtangjjigae.global.util.BaseEntity;
import com.example.japtangjjigae.user.common.OAuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String socialId;
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OAuthProvider oauthProvider;
    private String name;

    @Column(unique = true)
    private String phone;

    public static User createUser(String socialId, OAuthProvider oAuthProvider, String name, String phone){
        User newUser = new User();
        newUser.socialId = socialId;
        newUser.oauthProvider = oAuthProvider;
        newUser.name = name;
        newUser.phone = phone;

        return newUser;
    }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public OAuthProvider getOauthProvider(){
        return oauthProvider;
    }
}
