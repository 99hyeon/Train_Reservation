package com.example.japtangjjigae.jwt;

import com.example.japtangjjigae.oauth2.CustomOAuth2User;
import com.example.japtangjjigae.user.common.OAuthProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveAccessToken(request);

        if (accessToken == null || jwtUtil.isExpired(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        if(jwtUtil.getCategory(accessToken) != TokenCategory.ACCESS){
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtUtil.getUserId(accessToken);
        OAuthProvider oAuthProvider = jwtUtil.getOAuthProvider(accessToken);

        String principal = oAuthProvider + ":" + userId.toString();
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(
            Collections.emptyMap(),
            principal,
            userId,
            oAuthProvider,
            null
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            customOAuth2User,
            null,
            customOAuth2User.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);

        if(header == null || header.isBlank()) return null;

        if(!header.startsWith(BEARER_PREFIX)) return null;

        return header.substring(7);
    }

}
