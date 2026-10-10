package com.dynamic.product.user.service;

import com.dynamic.product.user.dto.UserProfileResponse;
import com.dynamic.product.user.entity.AppUser;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.user.mapper.UserProfileMapper;
import com.dynamic.product.user.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileMapper userProfileMapper;


    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, UserProfileMapper userProfileMapper) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userProfileMapper = userProfileMapper;
    }

    public UserProfileResponse getMyProfile(){
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new CustomException("Authentication required", HttpStatus.UNAUTHORIZED);
        }

        String email = authentication.getName();

        AppUser appUser=appUserRepository.findByEmail(email).orElseThrow(
                ()->new CustomException("User not found", HttpStatus.NOT_FOUND)
        );

        return userProfileMapper.toResponse(appUser);
    }
}
