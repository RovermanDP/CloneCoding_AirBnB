package com.airnest.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.airnest.backend.auth.dto.LoginRequest;
import com.airnest.backend.auth.dto.LoginResponse;
import com.airnest.backend.auth.entity.AppUser;
import com.airnest.backend.auth.entity.UserRole;
import com.airnest.backend.auth.repository.AppUserRepository;
import com.airnest.backend.auth.security.JwtTokenProvider;
import com.airnest.backend.auth.security.TokenBundle;
import com.airnest.backend.common.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginIssuesTokenForValidUser() {
        AppUser user = AppUser.create(
            "host@airnest.local",
            "encoded-password",
            "Emiel Jacobs",
            UserRole.HOST,
            true,
            Instant.now(),
            Instant.now()
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        when(appUserRepository.findByEmailIgnoreCase("host@airnest.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("host1234!", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.issueToken(any(AppUser.class))).thenReturn(new TokenBundle("token-value", Instant.parse("2026-03-26T10:00:00Z")));

        LoginResponse response = authService.login(new LoginRequest("host@airnest.local", "host1234!"));

        assertThat(response.accessToken()).isEqualTo("token-value");
        assertThat(response.user().email()).isEqualTo("host@airnest.local");
        assertThat(response.user().role()).isEqualTo("HOST");
    }

    @Test
    void loginRejectsInvalidPassword() {
        AppUser user = AppUser.create(
            "host@airnest.local",
            "encoded-password",
            "Emiel Jacobs",
            UserRole.HOST,
            true,
            Instant.now(),
            Instant.now()
        );

        when(appUserRepository.findByEmailIgnoreCase("host@airnest.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("host@airnest.local", "wrong-password")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Email or password is invalid.");
    }
}
