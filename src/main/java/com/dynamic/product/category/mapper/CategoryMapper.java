package com.dynamic.product.category.mapper;

import com.dynamic.product.category.dto.CategoryResponse;
import com.dynamic.product.category.entity.Category;
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