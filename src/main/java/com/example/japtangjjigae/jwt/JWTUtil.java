package com.example.japtangjjigae.jwt;

import com.example.japtangjjigae.user.common.OAuthProvider;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
            SIG.HS256.key().build().getAlgorithm());
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public Long getUserId(String token) {
        return jwtParser.parseSignedClaims(token).getPayload().get("userId", Long.class);
    }

    public String getUsername(String token) {
        return jwtParser.parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public OAuthProvider getOAuthProvider(String token) {
        String provider = jwtParser.parseSignedClaims(token).getPayload()
            .get("oAuthProvider", String.class);

        return OAuthProvider.valueOf(provider);
    }

    public boolean isExpired(String token) {
        return jwtParser.parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createJwt(Long userId, String username, OAuthProvider oAuthProvider,
        long expiredSeconds) {
        return Jwts.builder()
            .claim("userId", userId)
            .claim("username", username)
            .claim("oAuthProvider", oAuthProvider.name())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiredSeconds * 1000))
            .signWith(secretKey)
            .compact();
    }

}
