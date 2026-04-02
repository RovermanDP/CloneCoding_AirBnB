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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

        String refreshToken = generateOpaqueToken();
        String refreshTokenHash = sha256Hex(refreshToken);
        Instant refreshExpiresAt = issuedAt.plus(jwtProperties.getRefreshTokenTtl());

        return new TokenBundle(accessToken, expiresAt, refreshToken, refreshTokenHash, refreshExpiresAt);
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

    public Instant getExpiry(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return claims.getExpiration().toInstant();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Token is invalid or expired.");
        }
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
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
