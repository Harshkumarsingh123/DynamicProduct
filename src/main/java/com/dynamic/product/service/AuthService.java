package com.dynamic.product.service;

import com.dynamic.product.dto.request.LoginRequest;
import com.dynamic.product.dto.response.LoginResponse;
import com.dynamic.product.dto.request.RegisterRequest;
import com.dynamic.product.entity.AppUser;
import com.dynamic.product.entity.Role;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
                       CustomUserDetailsService customUserDetailsService,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService){
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String createUser(RegisterRequest registerRequest){

        if (appUserRepository.existsByEmail(registerRequest.getEmail())) {
            throw new CustomException("Email already registered", HttpStatus.CONFLICT);
        }

        AppUser appUser =new AppUser();
        appUser.setName(registerRequest.getName());
        appUser.setEmail(registerRequest.getEmail());
        appUser.setPhone(registerRequest.getPhone());
        appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        appUser.setRole(Role.USER);

        AppUser savedAppUser = appUserRepository.save(appUser);
        return "AppUser registered successfully";
    }

    public LoginResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        }
        catch (BadCredentialsException exception){
            throw new CustomException("Invalid Credentials",HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token);
    }
}
