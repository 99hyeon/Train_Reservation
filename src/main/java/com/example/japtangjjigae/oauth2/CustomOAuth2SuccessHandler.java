package com.example.japtangjjigae.oauth2;

import com.example.japtangjjigae.jwt.JWTUtil;
import com.example.japtangjjigae.jwt.TokenCategory;
import com.example.japtangjjigae.jwt.TokenCookie;
import com.example.japtangjjigae.jwt.TokenTTL;
import com.example.japtangjjigae.token.entity.RefreshToken;
import com.example.japtangjjigae.token.repository.RefreshTokenRepository;
import com.example.japtangjjigae.user.common.OAuthProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String COOKIE_HEADER = "Set-Cookie";

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof CustomOAuth2User customUserDetails)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        String signupTicket = customUserDetails.getSignupTicket();
        if (signupTicket != null) {
            writeJson(response, HttpServletResponse.SC_OK, Map.of(
                "status", "NEED_SIGNUP",
                "ticket", signupTicket
            ));
            return;
        }

        OAuthProvider oAuthProvider = customUserDetails.getoAuthProvider();
        Long userId = customUserDetails.getUserId();

        String accessToken = issueTokens(response, userId, oAuthProvider);
        writeJson(response, HttpServletResponse.SC_OK, Map.of(
            "status", "LOGIN_SUCCESS",
            "accessToken", accessToken
        ));
    }


    private String issueTokens(HttpServletResponse response, Long userId,
        OAuthProvider oAuthProvider) {
        String refreshToken = jwtUtil.createJwt(TokenCategory.REFRESH, userId, oAuthProvider,
            TokenTTL.REFRESH.seconds());
        String accessToken =
            BEARER_PREFIX + jwtUtil.createJwt(TokenCategory.ACCESS, userId, oAuthProvider,
                TokenTTL.ACCESS.seconds());

        response.setHeader(AUTH_HEADER, accessToken);
        response.addHeader(COOKIE_HEADER,
            TokenCookie.buildRefreshCookie(refreshToken, TokenTTL.REFRESH.seconds()));

        saveRefreshToken(userId, refreshToken);
        return accessToken;
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken forSaveRefresh = RefreshToken.createRefreshToken(userId, refreshToken,
            Instant.now().plus(TokenTTL.REFRESH.getDuration()).toString());
        refreshTokenRepository.save(forSaveRefresh);
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
