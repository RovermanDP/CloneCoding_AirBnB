package com.airnest.backend.auth.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 로그아웃된 Access Token을 만료 전까지 차단하는 인메모리 블랙리스트.
 *
 * <p>토큰이 만료되면 자동으로 정리됩니다 (매 10분마다).
 */
@Component
public class TokenBlacklist {

    // token → expiresAt
    private final ConcurrentHashMap<String, Instant> blacklisted = new ConcurrentHashMap<>();

    public void add(String token, Instant expiresAt) {
        blacklisted.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        Instant expiresAt = blacklisted.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklisted.remove(token);
            return false;
        }
        return true;
    }

    @Scheduled(fixedDelay = 600_000) // 10분마다 만료된 항목 정리
    public void evictExpired() {
        Instant now = Instant.now();
        blacklisted.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
