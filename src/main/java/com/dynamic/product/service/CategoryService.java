package com.dynamic.product.service;

import com.dynamic.product.dto.CategoryRequest;
import com.dynamic.product.dto.CategoryResponse;
import com.dynamic.product.entity.Category;
import com.dynamic.product.exception.CategoryNotFoundException;
import com.dynamic.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    public CategoryResponse saveCategory(CategoryRequest categoryRequest) {
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        Category parentCategory = null;


        if (categoryRequest.getParentId() != null) {
            Optional<Category> parentOpt = categoryRepository.findById(categoryRequest.getParentId());
            if (parentOpt.isEmpty()) {
                throw new IllegalArgumentException("Parent category with ID "
                        + categoryRequest.getParentId() + " not found");
            }
            parentCategory = parentOpt.get();
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setParent(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null
        );
    }

    public CategoryResponse updateCategory(Long id,CategoryRequest categoryRequest){

        Category category=categoryRepository.findById(id)
                .orElseThrow(
                ()->new CategoryNotFoundException("Invalid Category Id"));

        Category parentCategory = null;

        if (categoryRequest.getParentId() != null) {
            Optional<Category> parentOpt = categoryRepository.findById(categoryRequest.getParentId());
            if (parentOpt.isEmpty()) {
                throw new IllegalArgumentException("Parent category with ID "
                        + categoryRequest.getParentId() + " not found");
            }
            parentCategory = parentOpt.get();
        }

        if(categoryRequest.getName()!=null)
            category.setName(categoryRequest.getName());
        if(categoryRequest.getDescription()!=null)
            category.setDescription(categoryRequest.getDescription());

        category.setParent(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    public CategoryResponse getCategoryByCategoryId(Long id){

         Category category=categoryRepository.findById(id).orElseThrow(
                 ()->new CategoryNotFoundException("Invalid Category Id"));

          return mapToResponse(category);
    }

    public List<CategoryResponse> getAllCategory(){

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public void deleteCategoryByCategoryId(Long id){
        try {
            categoryRepository.deleteById(id);
        }
        catch (CategoryNotFoundException categoryNotFoundException){
            throw new CategoryNotFoundException("Invalid Category Id");
        }
    }
}
