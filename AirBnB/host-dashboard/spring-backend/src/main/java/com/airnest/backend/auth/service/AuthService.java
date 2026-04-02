package com.airnest.backend.auth.service;

import com.airnest.backend.auth.dto.AuthenticatedUserResponse;
import com.airnest.backend.auth.dto.LoginRequest;
import com.airnest.backend.auth.dto.LoginResponse;
import com.airnest.backend.auth.entity.AppUser;
import com.airnest.backend.auth.entity.RefreshToken;
import com.airnest.backend.auth.repository.AppUserRepository;
import com.airnest.backend.auth.repository.RefreshTokenRepository;
import com.airnest.backend.auth.security.AuthenticatedUserPrincipal;
import com.airnest.backend.auth.security.JwtTokenProvider;
import com.airnest.backend.auth.security.TokenBlacklist;
import com.airnest.backend.auth.security.TokenBundle;
import com.airnest.backend.common.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(
        AppUserRepository appUserRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        TokenBlacklist tokenBlacklist
    ) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Instant now = Instant.now();

        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
            .filter(AppUser::isActive)
            .orElseThrow(() -> new UnauthorizedException("Email or password is invalid."));

        if (user.isLocked(now)) {
            throw new UnauthorizedException("Account is temporarily locked. Please try again later.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.recordFailedLogin(now);
            throw new UnauthorizedException("Email or password is invalid.");
        }

        user.resetLoginAttempts(now);

        TokenBundle tokenBundle = jwtTokenProvider.issueToken(user);

        RefreshToken refreshToken = RefreshToken.create(
            user.getId(),
            tokenBundle.refreshTokenHash(),
            tokenBundle.refreshExpiresAt(),
            now
        );
        refreshTokenRepository.save(refreshToken);

        return new LoginResponse(
            tokenBundle.accessToken(),
            "Bearer",
            tokenBundle.expiresAt(),
            tokenBundle.refreshToken(),
            tokenBundle.refreshExpiresAt(),
            AuthenticatedUserResponse.from(user)
        );
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        Instant now = Instant.now();
        String tokenHash = JwtTokenProvider.sha256Hex(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if (!storedToken.isValid(now)) {
            throw new UnauthorizedException("Refresh token is expired or revoked.");
        }

        AppUser user = appUserRepository.findByIdAndActiveTrue(storedToken.getUserId())
            .orElseThrow(() -> new UnauthorizedException("User not found or inactive."));

        // Revoke old token and issue new one (token rotation)
        storedToken.revoke();

        TokenBundle tokenBundle = jwtTokenProvider.issueToken(user);
        RefreshToken newRefreshToken = RefreshToken.create(
            user.getId(),
            tokenBundle.refreshTokenHash(),
            tokenBundle.refreshExpiresAt(),
            now
        );
        refreshTokenRepository.save(newRefreshToken);

        return new LoginResponse(
            tokenBundle.accessToken(),
            "Bearer",
            tokenBundle.expiresAt(),
            tokenBundle.refreshToken(),
            tokenBundle.refreshExpiresAt(),
            AuthenticatedUserResponse.from(user)
        );
    }

    @Transactional
    public void logout(String rawAccessToken, String rawRefreshToken) {
        Instant accessTokenExpiry = jwtTokenProvider.getExpiry(rawAccessToken);
        tokenBlacklist.add(rawAccessToken, accessTokenExpiry);

        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = JwtTokenProvider.sha256Hex(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(RefreshToken::revoke);
        }
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse me(AuthenticatedUserPrincipal principal) {
        AppUser user = appUserRepository.findByIdAndActiveTrue(principal.id())
            .orElseThrow(() -> new UnauthorizedException("Authenticated user is not available."));
        return AuthenticatedUserResponse.from(user);
    }
}
