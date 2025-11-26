package com.example.japtangjjigae.oauth2;

import com.example.japtangjjigae.user.dto.UserDTO;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String principalName;
    private final String signupTicket;
    private final UserDTO userDto;

    public CustomOAuth2User(Map<String, Object> attributes, String principalName,
        String signupTicket, UserDTO userDto) {
        this.attributes = attributes;
        this.principalName = principalName;
        this.signupTicket = signupTicket;
        this.userDto = userDto;
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

    public UserDTO getUserDto(){
        return this.userDto;
    }
}
