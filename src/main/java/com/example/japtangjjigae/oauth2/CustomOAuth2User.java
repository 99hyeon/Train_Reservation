package com.example.japtangjjigae.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

//@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String principalName;
    private final String signupTicket;

    public CustomOAuth2User(Map<String, Object> attributes, String principalName,
        String signupTicket) {
        this.attributes = attributes;
        this.principalName = principalName;
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

    public Long getId(){
        String[] principals = principalName.split(":");

        return Long.parseLong(principals[1]);
    }

    public String getSignupTicket(){
        return this.signupTicket;
    }

}
