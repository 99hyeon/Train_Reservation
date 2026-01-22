package com.example.japtangjjigae.oauth2;

import com.example.japtangjjigae.user.common.OAuthProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String principalName;
    private final Long userId;
    private final OAuthProvider oAuthProvider;
    private final String signupTicket;

    public CustomOAuth2User(Map<String, Object> attributes, String principalName, Long userId,
        OAuthProvider oAuthProvider, String signupTicket) {
        this.attributes = attributes;
        this.principalName = principalName;
        this.userId = userId;
        this.oAuthProvider = oAuthProvider;
        this.signupTicket = signupTicket;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return this.principalName;
    }

    public Long getUserId(){
        return this.userId;
    }

    public OAuthProvider getoAuthProvider(){
        return this.oAuthProvider;
    }

    public String getSignupTicket(){
        return this.signupTicket;
    }

}
