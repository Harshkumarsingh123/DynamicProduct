package com.dynamic.product.auth.service;

import com.dynamic.product.auth.entity.AccountStatus;
import com.dynamic.product.auth.entity.AppUser;
import com.dynamic.product.auth.entity.OtpData;
import com.dynamic.product.auth.repository.AppUserRepository;
import com.dynamic.product.common.email.EmailService;
import com.dynamic.product.exception.CustomException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OtpService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long OTP_VALIDITY_MINUTES = 10;

    private final SecureRandom secureRandom = new SecureRandom();

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppUserRepository appUserRepository;
    private final OtpStore otpStore;

    public OtpService(
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            AppUserRepository appUserRepository,
            OtpStore otpStore) {

        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.appUserRepository = appUserRepository;
        this.otpStore = otpStore;
    }

    private String generateOtp() {
        return String.format(
                "%06d", secureRandom.nextInt(1_000_000));
    }

    public void createOtp(String email) {

        final String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();

        OtpData otpData = new OtpData(
                passwordEncoder.encode(otp),
                now,
                now.plusMinutes(OTP_VALIDITY_MINUTES),
                0,
                false
        );

        otpStore.save(normalizedEmail, otpData);

        try {
            CompletableFuture<Void> future =
                    emailService.sendOtpEmail(normalizedEmail, otp);

            future.whenComplete((result, error) -> {
                if (error != null) {
                    otpStore.remove(normalizedEmail, otpData);
                }
            });

        } catch (RuntimeException exception) {
            otpStore.remove(normalizedEmail, otpData);
            throw exception;
        }
    }

    public void verifyOtp(String email, String submittedOtp) {

        final String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        AtomicReference<String> outcome =
                new AtomicReference<>("MISSING");

        otpStore.compute(normalizedEmail, (key, data) -> {

            if (data == null) {
                outcome.set("MISSING");
                return null;
            }

            LocalDateTime now = LocalDateTime.now();

            if (!now.isBefore(data.getExpiresAt())) {
                outcome.set("EXPIRED");
                return null;
            }

            if (data.isLocked()) {
                outcome.set("LOCKED");
                return data;
            }

            if (!passwordEncoder.matches(
                    submittedOtp, data.getOtpHash())) {

                int attempts = data.getFailedAttempts() + 1;

                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    outcome.set("LOCKED");

                    return new OtpData(
                            data.getOtpHash(),
                            data.getCreatedAt(),
                            data.getExpiresAt(),
                            attempts,
                            true
                    );
                }

                outcome.set("INVALID:" +
                        (MAX_FAILED_ATTEMPTS - attempts));

                return new OtpData(
                        data.getOtpHash(),
                        data.getCreatedAt(),
                        data.getExpiresAt(),
                        attempts,
                        false
                );
            }

            outcome.set("VERIFIED");
            return null; // Consume OTP after successful verification.
        });

        switch (outcome.get()) {

            case "MISSING" ->
                    throw new CustomException(
                            "No pending OTP found. Please request a new OTP.",
                            HttpStatus.BAD_REQUEST);

            case "EXPIRED" ->
                    throw new CustomException(
                            "OTP has expired. Please request a new OTP.",
                            HttpStatus.BAD_REQUEST);

            case "LOCKED" ->
                    throw new CustomException(
                            "Too many incorrect attempts. Please request a new OTP.",
                            HttpStatus.TOO_MANY_REQUESTS);

            case "VERIFIED" -> {
                AppUser user = appUserRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() -> new CustomException(
                                "User not found.",
                                HttpStatus.NOT_FOUND));

                user.setEmailVerified(true);
                user.setAccountStatus(AccountStatus.ACTIVE);
                appUserRepository.save(user);
            }

            default -> {
                int remaining = Integer.parseInt(
                        outcome.get().substring("INVALID:".length()));

                throw new CustomException(
                        "Invalid OTP. Attempts remaining: " + remaining,
                        HttpStatus.BAD_REQUEST);
            }
        }
    }
}