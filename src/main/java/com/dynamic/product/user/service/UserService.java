package com.dynamic.product.user.service;

import com.dynamic.product.auth.entity.AppUser;
import com.dynamic.product.auth.repository.AppUserRepository;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.user.dto.UpdateUserProfileRequest;
import com.dynamic.product.user.dto.UserProfileResponse;
import com.dynamic.product.user.entity.UserProfile;
import com.dynamic.product.user.mapper.UserProfileMapper;
import com.dynamic.product.user.repository.UserProfileRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserProfileRepository userProfileRepository;

    public UserService(
            AppUserRepository appUserRepository,
            UserProfileMapper userProfileMapper,
            UserProfileRepository userProfileRepository) {

        this.appUserRepository = appUserRepository;
        this.userProfileMapper = userProfileMapper;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new CustomException(
                    "Authentication required",
                    HttpStatus.UNAUTHORIZED
            );
        }

        String email = authentication.getName();

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        UserProfile userProfile =
                userProfileRepository.findByUserId(appUser.getId())
                        .orElseThrow(() -> new CustomException(
                                "User profile not found",
                                HttpStatus.NOT_FOUND
                        ));

        return userProfileMapper.toResponse(userProfile);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(
            UpdateUserProfileRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new CustomException(
                    "Authentication required",
                    HttpStatus.UNAUTHORIZED
            );
        }

        String email = authentication.getName();

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        UserProfile userProfile =
                userProfileRepository.findByUserId(appUser.getId())
                        .orElseThrow(() -> new CustomException(
                                "User profile not found",
                                HttpStatus.NOT_FOUND
                        ));

        userProfile.setFullName(request.getFullName());
        userProfile.setPhone(request.getPhone());
        userProfile.setProfilePictureUrl(request.getProfileImageUrl());
        userProfile.setBio(request.getBio());

        return userProfileMapper.toResponse(userProfile);
    }
}