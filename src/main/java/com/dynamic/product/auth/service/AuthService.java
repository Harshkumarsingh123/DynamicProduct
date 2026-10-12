package com.dynamic.product.auth.service;

import com.dynamic.product.auth.dto.LoginRequest;
import com.dynamic.product.auth.dto.LoginResponse;
import com.dynamic.product.auth.dto.RegisterRequest;
import com.dynamic.product.auth.entity.AppUser;
import com.dynamic.product.auth.entity.Role;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.auth.repository.AppUserRepository;
import com.dynamic.product.user.entity.UserProfile;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
                       CustomUserDetailsService customUserDetailsService,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService, OtpService otpService){
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Transactional
    public String createUser(RegisterRequest registerRequest) {

        String email = registerRequest.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByEmail(email)) {
            throw new CustomException(
                    "Email already registered",
                    HttpStatus.CONFLICT
            );
        }

        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setPassword(
                passwordEncoder.encode(registerRequest.getPassword())
        );
        appUser.setRole(Role.USER);
        appUser.setEmailVerified(false);

        AppUser savedUser = appUserRepository.save(appUser);

        UserProfile profile = new UserProfile();
        profile.setFullName(email);

        savedUser.setProfile(profile);

        appUserRepository.save(savedUser);

        otpService.createOtp(email);

        return "Registration successful. Please check your email "
                + "for the OTP to verify your account.";
    }

    public LoginResponse login(LoginRequest request) {

        String email=request.getEmail().trim().toLowerCase(Locale.ROOT);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );
        }
        catch (DisabledException disabledException){
             throw new CustomException("Please verify your email before logging",HttpStatus.FORBIDDEN);
        }
        catch (BadCredentialsException exception){
            throw new CustomException("Invalid Credentials",HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token);
    }
}
