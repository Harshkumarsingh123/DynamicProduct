package com.dynamic.product.mapper;

import com.dynamic.product.dto.response.CategoryResponse;
import com.dynamic.product.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParent() != null
                        ? category.getParent().getId()
                        : null
        );
    }
}