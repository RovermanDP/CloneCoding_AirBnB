package com.airnest.backend.auth.service;

import com.airnest.backend.auth.dto.AuthenticatedUserResponse;
import com.airnest.backend.auth.dto.LoginRequest;
import com.airnest.backend.auth.dto.LoginResponse;
import com.airnest.backend.auth.entity.AppUser;
import com.airnest.backend.auth.repository.AppUserRepository;
import com.airnest.backend.auth.security.AuthenticatedUserPrincipal;
import com.airnest.backend.auth.security.JwtTokenProvider;
import com.airnest.backend.auth.security.TokenBundle;
import com.airnest.backend.common.exception.UnauthorizedException;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
            .filter(AppUser::isActive)
            .orElseThrow(() -> new UnauthorizedException("Email or password is invalid."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Email or password is invalid.");
        }

        TokenBundle tokenBundle = jwtTokenProvider.issueToken(user);
        return new LoginResponse(
            tokenBundle.accessToken(),
            "Bearer",
            tokenBundle.expiresAt(),
            AuthenticatedUserResponse.from(user)
        );
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse me(AuthenticatedUserPrincipal principal) {
        AppUser user = appUserRepository.findByIdAndActiveTrue(principal.id())
            .orElseThrow(() -> new UnauthorizedException("Authenticated user is not available."));
        return AuthenticatedUserResponse.from(user);
    }
}
