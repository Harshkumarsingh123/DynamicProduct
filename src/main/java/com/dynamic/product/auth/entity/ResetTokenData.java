package com.dynamic.product.auth.entity;

import java.time.LocalDateTime;

public class ResetTokenData {

    private final String email;
    private final LocalDateTime expiresAt;
    private final LocalDateTime cleanupAt;

    public ResetTokenData(
            String email,
            LocalDateTime expiresAt,
            LocalDateTime cleanupAt) {
        this.email = email;
        this.expiresAt = expiresAt;
        this.cleanupAt = cleanupAt;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCleanupAt() {
        return cleanupAt;
    }
}