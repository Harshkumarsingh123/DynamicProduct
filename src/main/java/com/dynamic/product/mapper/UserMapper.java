package com.dynamic.product.mapper;

import com.dynamic.product.dto.response.UserResponse;
import com.dynamic.product.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(AppUser appUser) {

        return new UserResponse(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),
                appUser.getPhone()
        );
    }
}