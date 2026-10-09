package com.dynamic.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterRequest {

    private Long categoryId;
    private Double minPrice;
    private Double maxPrice;
    private String keyword;
    private String sortBy;
    private String sortDirection;
    private Integer page=0;
    private Integer size=10;


}
