
package com.dynamic.product.user.controller;

import com.dynamic.product.common.response.ApiResponse;
import com.dynamic.product.user.dto.AddAddressRequest;
import com.dynamic.product.user.dto.AddressResponse;
import com.dynamic.product.user.dto.UpdateUserProfileRequest;
import com.dynamic.product.user.dto.UserProfileResponse;
import com.dynamic.product.user.service.AddressService;
import com.dynamic.product.user.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    public UserController(
            UserService userService,
            AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {

        UserProfileResponse profile = userService.getMyProfile();

        ApiResponse<UserProfileResponse> response =
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User profile fetched successfully",
                        profile
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest profileRequest) {

        UserProfileResponse profile =
                userService.updateUserProfile(profileRequest);

        ApiResponse<UserProfileResponse> response =
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User profile updated successfully",
                        profile
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddAddressRequest request) {

        AddressResponse address = addressService.addAddress(request);

        ApiResponse<AddressResponse> response =
                ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Address added successfully",
                        address
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
    
    @PutMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddAddressRequest request) {

        AddressResponse address =
                addressService.updateAddress(addressId, request);

        ApiResponse<AddressResponse> response =
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Address updated successfully",
                        address
                );

        return ResponseEntity.ok(response);
    }
}
