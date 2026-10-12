package com.dynamic.product.auth.entity;

import java.time.LocalDateTime;

public class OtpData {

    private final String otpHash;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final int failedAttempts;
    private final boolean locked;

    public OtpData(
            String otpHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            int failedAttempts,
            boolean locked) {

        this.otpHash = otpHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean isLocked() {
        return locked;
    }
}