package com.dynamic.product.auth.service;

import com.dynamic.product.auth.entity.Otp;
import com.dynamic.product.auth.repository.OtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final OtpRepository otpRepository;

    public OtpAttemptService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(Otp otp) {

        Otp currentOtp = otpRepository.findByIdForUpdate(otp.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "OTP record not found."
                ));

        if (Boolean.TRUE.equals(currentOtp.getLocked())
                || Boolean.TRUE.equals(currentOtp.getVerified())) {
            return MAX_FAILED_ATTEMPTS;
        }

        int attempts = currentOtp.getFailedAttempts() + 1;
        currentOtp.setFailedAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            currentOtp.setLocked(true);
        }

        otpRepository.save(currentOtp);

        return attempts;
    }
}