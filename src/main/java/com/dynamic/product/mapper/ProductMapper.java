package com.dynamic.product.mapper;

import com.dynamic.product.dto.response.ProductResponse;
import com.dynamic.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}