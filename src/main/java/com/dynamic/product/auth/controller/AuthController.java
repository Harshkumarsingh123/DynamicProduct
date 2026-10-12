
package com.dynamic.product.auth.controller;

import com.dynamic.product.auth.dto.LoginRequest;
import com.dynamic.product.auth.dto.LoginResponse;
import com.dynamic.product.auth.dto.RegisterRequest;
import com.dynamic.product.auth.dto.VerifyOtpRequest;
import com.dynamic.product.auth.service.AuthService;
import com.dynamic.product.auth.service.OtpService;
import com.dynamic.product.common.email.EmailService;
import com.dynamic.product.common.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
