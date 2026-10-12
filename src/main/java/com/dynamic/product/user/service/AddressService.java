package com.dynamic.product.user.service;

import com.dynamic.product.auth.entity.AppUser;
import com.dynamic.product.auth.repository.AppUserRepository;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.user.dto.AddAddressRequest;
import com.dynamic.product.user.dto.AddressResponse;
import com.dynamic.product.user.entity.Address;
import com.dynamic.product.user.entity.AddressType;
import com.dynamic.product.user.mapper.AddressMapper;
import com.dynamic.product.user.repository.AddressRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final AppUserRepository appUserRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    public AddressService(
            AppUserRepository appUserRepository,
            AddressRepository addressRepository,
            AddressMapper addressMapper) {

        this.appUserRepository = appUserRepository;
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    @Transactional
    public AddressResponse addAddress(AddAddressRequest request) {

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

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        Address address = new Address();

        address.setAddressType(request.getAddressType());
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

        address.setUser(user);

        boolean hasDefaultAddress =
                addressRepository
                        .findByUserIdAndDefaultAddressTrue(user.getId())
                        .isPresent();

        boolean makeDefault =
                request.isDefaultAddress() || !hasDefaultAddress;

        if (makeDefault) {
            addressRepository.findByUserIdAndDefaultAddressTrue(user.getId())
                    .ifPresent(existingAddress -> {
                        existingAddress.setDefaultAddress(false);
                        addressRepository.save(existingAddress);
                    });
        }

        address.setDefaultAddress(makeDefault);

        Address savedAddress = addressRepository.save(address);
        return addressMapper.toResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(
            Long addressId,
            AddAddressRequest request) {

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

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));
        Address address = addressRepository
                .findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new CustomException(
                        "Address not found",
                        HttpStatus.NOT_FOUND
                ));

        address.setAddressType(request.getAddressType());
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

        boolean hasDefaultAddress = addressRepository
                .findByUserIdAndDefaultAddressTrue(user.getId())
                .isPresent();

        boolean makeDefault = request.isDefaultAddress()
                || !hasDefaultAddress
                || address.isDefaultAddress();

        if (makeDefault) {
            addressRepository
                    .findByUserIdAndDefaultAddressTrue(user.getId())
                    .ifPresent(existingAddress -> {
                        if (!existingAddress.getId().equals(address.getId())) {
                            existingAddress.setDefaultAddress(false);
                        }
                    });
        }

        address.setDefaultAddress(makeDefault);

        Address updatedAddress = addressRepository.save(address);

        return addressMapper.toResponse(updatedAddress);
    }
}