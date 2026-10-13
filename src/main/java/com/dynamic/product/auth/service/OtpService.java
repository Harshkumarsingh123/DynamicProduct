package com.dynamic.product.auth.service;

import com.dynamic.product.auth.entity.AccountStatus;
import com.dynamic.product.auth.entity.AppUser;
import com.dynamic.product.auth.entity.OtpData;
import com.dynamic.product.auth.entity.OtpPurpose;
import com.dynamic.product.auth.entity.ResetTokenData;
import com.dynamic.product.auth.repository.AppUserRepository;
import com.dynamic.product.common.email.EmailService;
import com.dynamic.product.exception.CustomException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OtpService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long OTP_VALIDITY_MINUTES = 10;
    private static final long RESET_TOKEN_VALIDITY_MINUTES = 10;

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

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomException(
                    "Email is required.",
                    HttpStatus.BAD_REQUEST);
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateOtp() {
        return String.format(
                "%06d",
                secureRandom.nextInt(1_000_000));
    }

    public void createOtp(String email) {
        createOtpForPurpose(email, OtpPurpose.EMAIL_VERIFICATION);
    }


    private void createOtpForPurpose(
            String email,
            OtpPurpose purpose) {

        String normalizedEmail = normalizeEmail(email);

        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();

        OtpData otpData = new OtpData(
                passwordEncoder.encode(otp),
                now,
                now.plusMinutes(OTP_VALIDITY_MINUTES),
                0,
                false,
                now.plusHours(24),
                purpose
        );

        otpStore.save(normalizedEmail, otpData);

        try {
            CompletableFuture<Void> future =
                    emailService.sendOtpEmail(normalizedEmail, otp);

            future.whenComplete((result, error) -> {
                if (error != null) {
                    // Do not remove a newer OTP created for the same email.
                    otpStore.remove(normalizedEmail, otpData);
                }
            });

        } catch (RuntimeException exception) {
            otpStore.remove(normalizedEmail, otpData);
            throw exception;
        }
    }

    public void verifyOtp(String email, String submittedOtp) {

        String normalizedEmail = normalizeEmail(email);

        verifyOtpForPurpose(
                normalizedEmail,
                submittedOtp,
                OtpPurpose.EMAIL_VERIFICATION
        );

        AppUser user = appUserRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() -> new CustomException(
                        "User not found.",
                        HttpStatus.NOT_FOUND));

        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);

        appUserRepository.save(user);
    }

    public void createPasswordResetOtp(String email) {

        String normalizedEmail = normalizeEmail(email);

        if (appUserRepository.findByEmail(normalizedEmail).isEmpty()) {
            return;
        }

        createOtpForPurpose(
                normalizedEmail,
                OtpPurpose.PASSWORD_RESET
        );
    }

    private void verifyOtpForPurpose(
            String normalizedEmail,
            String submittedOtp,
            OtpPurpose expectedPurpose) {

        if (submittedOtp == null || !submittedOtp.matches("\\d{6}")) {
            throw new CustomException(
                    "OTP must contain exactly 6 digits.",
                    HttpStatus.BAD_REQUEST);
        }

        AtomicReference<String> outcome =
                new AtomicReference<>("MISSING");

        otpStore.compute(normalizedEmail, (key, data) -> {

            if (data == null) {
                outcome.set("MISSING");
                return null;
            }

            if (data.getPurpose() != expectedPurpose) {
                outcome.set("WRONG_PURPOSE");
                return data;
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
                            true,
                            data.getCleanupAt(),
                            data.getPurpose()
                    );
                }

                outcome.set("INVALID:" +
                        (MAX_FAILED_ATTEMPTS - attempts));

                return new OtpData(
                        data.getOtpHash(),
                        data.getCreatedAt(),
                        data.getExpiresAt(),
                        attempts,
                        false,
                        data.getCleanupAt(),
                        data.getPurpose()
                );
            }

            outcome.set("VERIFIED");

            return null;
        });

        switch (outcome.get()) {

            case "MISSING" ->
                    throw new CustomException(
                            "No pending OTP found. Please request a new OTP.",
                            HttpStatus.BAD_REQUEST);

            case "WRONG_PURPOSE" ->
                    throw new CustomException(
                            "OTP was issued for a different purpose.",
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

    public String verifyPasswordResetOtp(
            String email,
            String submittedOtp) {

        String normalizedEmail = normalizeEmail(email);

        verifyOtpForPurpose(
                normalizedEmail,
                submittedOtp,
                OtpPurpose.PASSWORD_RESET
        );

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String resetToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        LocalDateTime now = LocalDateTime.now();

        ResetTokenData tokenData = new ResetTokenData(
                normalizedEmail,
                now.plusMinutes(RESET_TOKEN_VALIDITY_MINUTES),
                now.plusHours(1)
        );

        otpStore.saveResetToken(
                sha256(resetToken),
                tokenData
        );

        // Return the raw token once to the client.
        return resetToken;
    }

    public void resetPassword(
            String resetToken,
            String newPassword,
            String confirmPassword) {

        if (resetToken == null || resetToken.isBlank()) {
            throw new CustomException(
                    "Reset token is required.",
                    HttpStatus.BAD_REQUEST);
        }

        if (newPassword == null || confirmPassword == null
                || !newPassword.equals(confirmPassword)) {
            throw new CustomException(
                    "New password and confirmation do not match.",
                    HttpStatus.BAD_REQUEST);
        }

        String tokenHash = sha256(resetToken);
        ResetTokenData tokenData =
                otpStore.consumeResetToken(tokenHash);

        if (tokenData == null) {
            throw new CustomException(
                    "Invalid or already-used reset token. Verify your OTP again.",
                    HttpStatus.BAD_REQUEST);
        }

        if (!LocalDateTime.now().isBefore(tokenData.getExpiresAt())) {
            throw new CustomException(
                    "Reset token has expired. Verify your OTP again.",
                    HttpStatus.BAD_REQUEST);
        }

        AppUser user = appUserRepository
                .findByEmail(tokenData.getEmail())
                .orElseThrow(() -> new CustomException(
                        "User not found.",
                        HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(
                newPassword, user.getPassword())) {
            throw new CustomException(
                    "New password must be different from the current password.",
                    HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available.", exception);
        }
    }
}