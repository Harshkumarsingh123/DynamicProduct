package com.dynamic.product.auth.controller;

import com.dynamic.product.auth.dto.LoginRequest;
import com.dynamic.product.auth.dto.VerifyOtpRequest;
import com.dynamic.product.common.response.ApiResponse;
import com.dynamic.product.auth.dto.LoginResponse;
import com.dynamic.product.auth.dto.RegisterRequest;
import com.dynamic.product.auth.service.AuthService;
import com.dynamic.product.common.email.EmailService;
import com.dynamic.product.auth.service.OtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthController(AuthService authService, OtpService otpService, EmailService emailService) {
        this.authService = authService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> createUser(
            @Valid @RequestBody RegisterRequest registerRequest){
        String message=authService.createUser(registerRequest);
        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.CREATED.value(),
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse=authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        otpService.verifyOtp(request.getEmail(), request.getOtp());

        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.OK.value(),
                "Email verified successfully. You can now log in.",
                LocalDateTime.now()
        );

        emailService.sendWelcomeEmail(request.getEmail());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(
            @RequestParam
            @NotBlank(message = "Email is required")
            @Email(message = "Please provide a valid email")
            String email) {

        otpService.createOtp(email);

        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.OK.value(),
                "If your account is eligible, an OTP will be sent to your email.",
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }

}
