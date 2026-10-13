package com.dynamic.product.auth.service;

import com.dynamic.product.auth.entity.OtpData;
import com.dynamic.product.auth.entity.ResetTokenData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Component
public class OtpStore {

    private final ConcurrentHashMap<String, OtpData> otpMap =
            new ConcurrentHashMap<>();

    // Key is the SHA-256 hash of the reset token, not the raw token.
    private final ConcurrentHashMap<String, ResetTokenData> resetTokenMap =
            new ConcurrentHashMap<>();

    public OtpData get(String email) {
        return otpMap.get(email);
    }

    public void save(String email, OtpData otpData) {
        otpMap.put(email, otpData);
    }

    public boolean remove(String email, OtpData otpData) {
        return otpMap.remove(email, otpData);
    }

    public OtpData compute(
            String email,
            BiFunction<String, OtpData, OtpData> function) {
        return otpMap.compute(email, function);
    }

    public void saveResetToken(
            String tokenHash,
            ResetTokenData tokenData) {
        resetTokenMap.put(tokenHash, tokenData);
    }

    public ResetTokenData getResetToken(String tokenHash) {
        return resetTokenMap.get(tokenHash);
    }

    public ResetTokenData consumeResetToken(String tokenHash) {
        return resetTokenMap.remove(tokenHash);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();

        otpMap.entrySet().removeIf(entry ->
                !now.isBefore(entry.getValue().getCleanupAt()));

        resetTokenMap.entrySet().removeIf(entry ->
                !now.isBefore(entry.getValue().getCleanupAt()));
    }
}