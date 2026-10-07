package com.dynamic.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @Positive(message = "Price  must be greater then 0")
    private double price;

    private boolean visible;

    private String imageUrl;

    @NotNull
    private long category_id;
}
