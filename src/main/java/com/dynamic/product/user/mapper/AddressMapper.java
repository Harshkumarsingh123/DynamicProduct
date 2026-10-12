package com.dynamic.product.user.mapper;

import com.dynamic.product.user.dto.AddressResponse;
import com.dynamic.product.user.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponse toResponse(Address address) {

        return new AddressResponse(
                address.getId(),
                address.getAddressType().name(),
                address.getRecipientName(),
                address.getPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
