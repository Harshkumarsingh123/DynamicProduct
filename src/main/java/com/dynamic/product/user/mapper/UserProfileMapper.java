package com.dynamic.product.user.mapper;

import com.dynamic.product.user.dto.UserProfileResponse;
import com.dynamic.product.user.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(AppUser appUser) {

        return new UserProfileResponse(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),
                appUser.getPhone(),
                appUser.getProfileImageUrl(),
                appUser.getEmailVerified(),
                appUser.getCreatedAt()
        );
    }
}
