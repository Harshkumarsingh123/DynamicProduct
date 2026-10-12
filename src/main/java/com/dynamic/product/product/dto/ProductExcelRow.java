package com.dynamic.product.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductExcelRow {
    private final int rowNumber;
    private final String name;
    private final String description;
    private final String price;
    private final String visible;
    private final String imageUrl;
    private final String categoryId;
}
