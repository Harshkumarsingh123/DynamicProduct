package com.dynamic.product.controller;

import com.dynamic.product.dto.request.CategoryRequest;
import com.dynamic.product.dto.response.CategoryResponse;
import com.dynamic.product.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> saveCategory(@Valid
                                                         @RequestBody CategoryRequest categoryRequest){
        CategoryResponse categoryResponse=categoryService.saveCategory(categoryRequest);
        return new ResponseEntity<>(categoryResponse, HttpStatus.CREATED);
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
    public ResponseEntity<CategoryResponse> updateCategoryById( @PathVariable Long id,
                           @Valid @RequestBody CategoryRequest categoryRequest){
        CategoryResponse categoryResponse=categoryService.updateCategory(id,categoryRequest);
        return ResponseEntity.ok(categoryResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable Long id){
        categoryService.deleteCategoryByCategoryId(id);
        return ResponseEntity.noContent().build();
    }
}
