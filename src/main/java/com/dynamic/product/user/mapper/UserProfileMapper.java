package com.dynamic.product.user.mapper;

import com.dynamic.product.user.dto.UserProfileResponse;
import com.dynamic.product.user.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile userProfile) {

        return new UserProfileResponse(
                userProfile.getId(),
                userProfile.getFullName(),
                userProfile.getPhone(),
                userProfile.getProfilePictureUrl(),
                userProfile.getBio(),
                userProfile.getCreatedAt()
        );
    }
}
