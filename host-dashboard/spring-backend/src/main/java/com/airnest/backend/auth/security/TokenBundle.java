package com.airnest.backend.auth.security;

import java.time.Instant;

public record TokenBundle(
    String accessToken,
    Instant expiresAt,
    String refreshToken,
    String refreshTokenHash,
    Instant refreshExpiresAt
) {
}
