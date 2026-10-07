package com.dynamic.product.mapper;

import com.dynamic.product.dto.response.UserResponse;
import com.dynamic.product.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone()
        );
    }
}