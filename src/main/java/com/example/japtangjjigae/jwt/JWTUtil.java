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

    private static final String CATEGORY = "category";
    private static final String USER_ID = "userId";
    private static final String OAUTH_PROVIDER = "oAuthProvider";

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            SIG.HS256.key().build().getAlgorithm()
        );
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public TokenCategory getCategory(String token) {
        String raw = jwtParser.parseSignedClaims(token).getPayload().get(CATEGORY, String.class);
        return TokenCategory.valueOf(raw);
    }

    public Long getUserId(String token) {
        return jwtParser.parseSignedClaims(token)
            .getPayload()
            .get(USER_ID, Long.class);
    }

    public OAuthProvider getOAuthProvider(String token) {
        String provider = jwtParser.parseSignedClaims(token)
            .getPayload()
            .get(OAUTH_PROVIDER, String.class);

        return OAuthProvider.valueOf(provider);
    }

    public boolean isExpired(String token) {
        Date expiration = jwtParser.parseSignedClaims(token)
            .getPayload()
            .getExpiration();

        return expiration.before(new Date());
    }

    public String createJwt(TokenCategory category, Long userId, OAuthProvider oAuthProvider,
        long expiredSeconds) {
        return Jwts.builder()
            .claim(CATEGORY, category.name())
            .claim(USER_ID, userId)
            .claim(OAUTH_PROVIDER, oAuthProvider.name())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiredSeconds * 1000))
            .signWith(secretKey)
            .compact();
    }
}
