package com.dynamic.product.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String profileImageUrl;
    private String bio;
    private LocalDateTime createdAt;
}
