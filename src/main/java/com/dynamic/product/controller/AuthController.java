package com.dynamic.product.controller;

import com.dynamic.product.dto.request.LoginRequest;
import com.dynamic.product.dto.response.ApiResponse;
import com.dynamic.product.dto.response.LoginResponse;
import com.dynamic.product.dto.request.RegisterRequest;
import com.dynamic.product.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
