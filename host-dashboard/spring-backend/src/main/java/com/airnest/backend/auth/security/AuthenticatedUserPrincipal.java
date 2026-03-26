package com.airnest.backend.auth.security;

import com.airnest.backend.auth.entity.UserRole;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthenticatedUserPrincipal(
    Long id,
    String email,
    String displayName,
    UserRole role
) {
    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }
}
