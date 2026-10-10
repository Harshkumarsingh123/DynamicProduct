package com.dynamic.product.category.controller;

import com.dynamic.product.category.dto.CategoryRequest;
import com.dynamic.product.common.response.ApiResponse;
import com.dynamic.product.category.dto.CategoryResponse;
import com.dynamic.product.category.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public ResponseEntity<ApiResponse> saveCategory(@Valid
                                                         @RequestBody CategoryRequest categoryRequest){
        String message=categoryService.saveCategory(categoryRequest);
        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.CREATED.value(),
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable  Long id){
        CategoryResponse categoryResponse=categoryService.getCategoryByCategoryId(id);
        return ResponseEntity.ok(categoryResponse);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categoryResponses = categoryService.getAllCategory();
        return ResponseEntity.ok(categoryResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategoryById( @PathVariable Long id,
                           @Valid @RequestBody CategoryRequest categoryRequest){
        String message=categoryService.updateCategory(id,categoryRequest);
        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategoryById(@PathVariable Long id){
         String message= categoryService.deleteCategoryByCategoryId(id);
        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
