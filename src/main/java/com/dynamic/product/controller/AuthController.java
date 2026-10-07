package com.dynamic.product.controller;

import com.dynamic.product.dto.request.LoginRequest;
import com.dynamic.product.dto.response.LoginResponse;
import com.dynamic.product.dto.request.UserRequest;
import com.dynamic.product.dto.response.UserResponse;
import com.dynamic.product.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest userRequest){
        UserResponse userResponse=authService.createUser(userRequest);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse=authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}
