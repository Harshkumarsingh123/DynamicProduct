
package com.dynamic.product.category.controller;

import com.dynamic.product.category.dto.CategoryRequest;
import com.dynamic.product.category.dto.CategoryResponse;
import com.dynamic.product.category.service.CategoryService;
import com.dynamic.product.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveCategory(
            @Valid @RequestBody CategoryRequest categoryRequest) {

        String message = categoryService.saveCategory(categoryRequest);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.CREATED.value(),
                message,
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long id) {

        CategoryResponse category =
                categoryService.getCategoryByCategoryId(id);

        ApiResponse<CategoryResponse> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Category fetched successfully",
                category
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>>
    getAllCategories() {

        List<CategoryResponse> categories =
                categoryService.getAllCategory();

        ApiResponse<List<CategoryResponse>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Categories fetched successfully",
                categories
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCategoryById(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        String message =
                categoryService.updateCategory(id, categoryRequest);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                message,
                null
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryById(
            @PathVariable Long id) {

        String message =
                categoryService.deleteCategoryByCategoryId(id);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                message,
                null
        );

        return ResponseEntity.ok(response);
    }
}
