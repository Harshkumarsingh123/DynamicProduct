
package com.dynamic.product.auth.controller;

import com.dynamic.product.auth.dto.*;
import com.dynamic.product.auth.service.AuthService;
import com.dynamic.product.auth.service.OtpService;
import com.dynamic.product.common.email.EmailService;
import com.dynamic.product.common.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthController(
            AuthService authService,
            OtpService otpService,
            EmailService emailService) {
        this.authService = authService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> createUser(
            @Valid @RequestBody RegisterRequest registerRequest) {

        String message = authService.createUser(registerRequest);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.CREATED.value(),
                message,
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse loginResponse = authService.login(loginRequest);

        ApiResponse<LoginResponse> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Login successful",
                loginResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        otpService.verifyOtp(request.getEmail(), request.getOtp());

        emailService.sendWelcomeEmail(request.getEmail());

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Email verified successfully. You can now log in.",
                null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @RequestParam
            @NotBlank(message = "Email is required")
            @Email(message = "Please provide a valid email")
            String email) {

        otpService.createOtp(email);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "If your account is eligible, an OTP will be sent to your email.",
                null
        );

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
                @Valid @RequestBody ChangePasswordRequest request,
                Authentication authentication) {

        String message = authService.changePassword(
                    authentication.getName(),
                    request
            );

        ApiResponse<Void> response = ApiResponse.success(
                    HttpStatus.OK.value(),
                    message,
                    null
            );

        return ResponseEntity.ok(response);
        }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        otpService.createPasswordResetOtp(request.getEmail());

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "If an account exists for this email, a password reset OTP has been sent.",
                null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<String>> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request) {

        String resetToken = otpService.verifyPasswordResetOtp(
                request.getEmail(),
                request.getOtp()
        );

        ApiResponse<String> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "OTP verified. Use the reset token to set a new password.",
                resetToken
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        otpService.resetPassword(
                request.getResetToken(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Password reset successfully.",
                null
        );

        return ResponseEntity.ok(response);
    }

}
