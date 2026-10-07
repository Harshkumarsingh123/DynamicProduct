package com.dynamic.product.service;

import com.dynamic.product.dto.request.LoginRequest;
import com.dynamic.product.dto.response.LoginResponse;
import com.dynamic.product.dto.request.UserRequest;
import com.dynamic.product.dto.response.UserResponse;
import com.dynamic.product.entity.Role;
import com.dynamic.product.entity.User;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.mapper.UserMapper;
import com.dynamic.product.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomUserDetailsService customUserDetailsService, AuthenticationManager authenticationManager, JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    public UserResponse createUser(UserRequest userRequest){

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new CustomException("Email already registered", HttpStatus.CONFLICT);
        }

        User user=new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.USER);

        User savedUser=userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(
                        request.getEmail()
                );

        String token =
                jwtService.generateToken(userDetails);

        return new LoginResponse(
                token
        );
    }
}
