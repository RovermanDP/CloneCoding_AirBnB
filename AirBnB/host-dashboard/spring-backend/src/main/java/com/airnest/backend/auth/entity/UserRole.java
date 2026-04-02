package com.airnest.backend.auth.entity;

public enum UserRole {
    HOST;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
