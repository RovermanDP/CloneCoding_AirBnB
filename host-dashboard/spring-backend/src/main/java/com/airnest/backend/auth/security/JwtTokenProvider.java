package com.airnest.backend.auth.security;

import com.airnest.backend.auth.config.JwtProperties;
import com.airnest.backend.auth.entity.AppUser;
import com.airnest.backend.auth.entity.UserRole;
import com.airnest.backend.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = buildSecretKey(jwtProperties.getSecret());
    }

    public TokenBundle issueToken(AppUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());

        String accessToken = Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .issuer(jwtProperties.getIssuer())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .claim("email", user.getEmail())
            .claim("displayName", user.getDisplayName())
            .claim("role", user.getRole().name())
            .signWith(secretKey)
            .compact();

        return new TokenBundle(accessToken, expiresAt);
    }

    public AuthenticatedUserPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return new AuthenticatedUserPrincipal(
                Long.parseLong(claims.getSubject()),
                claims.get("email", String.class),
                claims.get("displayName", String.class),
                UserRole.valueOf(claims.get("role", String.class))
            );
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Token is invalid or expired.");
        }
    }

    private SecretKey buildSecretKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.auth.jwt.secret must be configured.");
        }

        byte[] secretBytes = tryDecodeBase64(secret.trim());
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.auth.jwt.secret must be at least 32 bytes.");
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    private byte[] tryDecodeBase64(String value) {
        try {
            return Decoders.BASE64.decode(value);
        } catch (RuntimeException exception) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
    }
}
