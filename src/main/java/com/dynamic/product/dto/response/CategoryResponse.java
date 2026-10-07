package com.dynamic.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
}
