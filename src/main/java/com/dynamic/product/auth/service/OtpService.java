package com.dynamic.product.auth.service;

import com.dynamic.product.common.email.EmailService;
import com.dynamic.product.user.entity.AppUser;
import com.dynamic.product.auth.entity.Otp;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.user.repository.AppUserRepository;
import com.dynamic.product.auth.repository.OtpRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class OtpService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom=new SecureRandom();
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppUserRepository appUserRepository;
    private final OtpAttemptService otpAttemptService;

    public OtpService(OtpRepository otpRepository, PasswordEncoder passwordEncoder, EmailService emailService, AppUserRepository appUserRepository, OtpAttemptService otpAttemptService) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.appUserRepository = appUserRepository;
        this.otpAttemptService = otpAttemptService;
    }

    private String generateOtp(){
        int number=secureRandom.nextInt(1_000_000);
        return String.format("%06d",number);
    }

    public void createOtp(String email){
        String otp=generateOtp();
        LocalDateTime now=LocalDateTime.now();

        Otp otpEntity=new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtpHash(passwordEncoder.encode(otp));
        otpEntity.setCreatedAt(now);
        otpEntity.setVerified(false);
        otpEntity.setExpiresAt(now.plusMinutes(10));

        otpRepository.save(otpEntity);
        emailService.sendOtpEmail(email,otp);
    }


    @Transactional
    public void verifyOtp(String email, String submittedOtp) {

        email = email.trim().toLowerCase(Locale.ROOT);

        Otp otp = otpRepository
                .findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new CustomException(
                        "No pending OTP found. Please request a new OTP.",
                        HttpStatus.BAD_REQUEST
                ));

        if (Boolean.TRUE.equals(otp.getLocked())) {
            throw new CustomException(
                    "Too many incorrect attempts. Please request a new OTP.",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }

        if (!LocalDateTime.now().isBefore(otp.getExpiresAt())) {
            throw new CustomException(
                    "OTP has expired. Please request a new OTP.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!passwordEncoder.matches(submittedOtp, otp.getOtpHash())) {

            int attempts = otpAttemptService.recordFailedAttempt(otp);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                throw new CustomException(
                        "Too many incorrect attempts. Please request a new OTP.",
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }

            throw new CustomException(
                    "Invalid OTP. Attempts remaining: "
                            + (MAX_FAILED_ATTEMPTS - attempts),
                    HttpStatus.BAD_REQUEST
            );
        }

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found.",
                        HttpStatus.NOT_FOUND
                ));

        otp.setVerified(true);
        appUser.setEmailVerified(true);

        otpRepository.save(otp);
        appUserRepository.save(appUser);
    }


}
