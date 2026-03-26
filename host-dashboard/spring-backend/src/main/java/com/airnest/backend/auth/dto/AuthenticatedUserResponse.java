package com.airnest.backend.auth.dto;

import com.airnest.backend.auth.entity.AppUser;

public record AuthenticatedUserResponse(
    Long id,
    String email,
    String displayName,
    String role
) {
    public static AuthenticatedUserResponse from(AppUser user) {
        return new AuthenticatedUserResponse(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getRole().name()
        );
    }
}
