package com.dynamic.product.user.controller;

import com.dynamic.product.user.dto.AddAddressRequest;
import com.dynamic.product.user.dto.AddressResponse;
import com.dynamic.product.user.service.AddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/addresses")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @Valid @RequestBody AddAddressRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.addAddress(request));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddAddressRequest request) {

        return ResponseEntity.ok(
                addressService.updateAddress(addressId, request)
        );
    }
}