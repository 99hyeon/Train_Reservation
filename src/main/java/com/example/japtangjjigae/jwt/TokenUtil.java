package com.example.japtangjjigae.jwt;

import com.example.japtangjjigae.oauth2.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TokenUtil {

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomOAuth2User u) {
            Long userId = u.getUserId();
            if (userId == null) {
                throw new IllegalStateException("UserId == null");
            }
            return userId;
        }

        throw new IllegalStateException("Unsupported principal: " + principal.getClass());
    }

}
