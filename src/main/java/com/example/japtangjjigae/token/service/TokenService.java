package com.example.japtangjjigae.token.service;

import com.example.japtangjjigae.exception.TokenNotFoundException;
import com.example.japtangjjigae.global.response.code.TokenResponseCode;
import com.example.japtangjjigae.jwt.JWTUtil;
import com.example.japtangjjigae.jwt.TokenCategory;
import com.example.japtangjjigae.jwt.TokenCookie;
import com.example.japtangjjigae.jwt.TokenTTL;
import com.example.japtangjjigae.token.entity.RefreshToken;
import com.example.japtangjjigae.token.repository.RefreshTokenRepository;
import com.example.japtangjjigae.user.common.OAuthProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public String[] issueAccessTokenRefreshToken(HttpServletRequest request) {
        String refreshToken = extractRefreshFromCookie(request);
        checkRefreshTokenIsValid(refreshToken);

        Long userId = jwtUtil.getUserId(refreshToken);
        OAuthProvider oAuthProvider = jwtUtil.getOAuthProvider(refreshToken);

        String newAccessToken = "Bearer " + jwtUtil.createJwt(TokenCategory.ACCESS, userId, oAuthProvider,
            TokenTTL.ACCESS.seconds());
        String newRefreshToken = jwtUtil.createJwt(TokenCategory.REFRESH, userId, oAuthProvider,
            TokenTTL.REFRESH.seconds());

        refreshTokenRotation(refreshToken, userId, newRefreshToken);

        return new String[]{newAccessToken, newRefreshToken};
    }

    private void refreshTokenRotation(String refreshToken, Long userId, String newRefreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
        addRefreshToken(userId, newRefreshToken);
    }

    private void addRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = RefreshToken.createRefreshToken(
            userId,
            newRefreshToken,
            Instant.now().plus(TokenTTL.REFRESH.getDuration()).toString()
            );

        refreshTokenRepository.save(refreshToken);
    }

    private static String extractRefreshFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TokenCookie.REFRESH_NAME)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void checkRefreshTokenIsValid(String refresh) {
        if (refresh == null) {
            throw new TokenNotFoundException(TokenResponseCode.REFRESH_TOKEN_NOT_FOUND);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException ex) {
            throw new TokenNotFoundException(TokenResponseCode.REFRESH_TOKEN_EXPIRED);
        }

        if (!TokenCategory.REFRESH.equals(jwtUtil.getCategory(refresh))
            || !refreshTokenRepository.existsByToken(refresh)) {
            throw new TokenNotFoundException(TokenResponseCode.REFRESH_TOKEN_NOT_FOUND);
        }
    }

}
