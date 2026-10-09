package com.dynamic.product.service;

import com.dynamic.product.dto.request.CategoryRequest;
import com.dynamic.product.dto.response.CategoryResponse;
import com.dynamic.product.entity.Category;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.mapper.CategoryMapper;
import com.dynamic.product.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }


    public String saveCategory(CategoryRequest categoryRequest) {
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new CustomException("Category name cannot be null or empty",HttpStatus.BAD_REQUEST);
        }

        Category parentCategory = null;


        if (categoryRequest.getParentId() != null) {
            Optional<Category> parentOpt = categoryRepository.findById(categoryRequest.getParentId());
            if (parentOpt.isEmpty()) {
                throw new CustomException(
                        "Parent category with ID " +
                                categoryRequest.getParentId() +
                                " not found",
                        HttpStatus.NOT_FOUND
                );
            }
            parentCategory = parentOpt.get();
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setParent(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return "Category created successfully";
    }


    public String updateCategory(Long id,CategoryRequest categoryRequest){

        Category category=categoryRepository.findById(id)
                .orElseThrow(
                ()->new CustomException( "Category with ID " + id + " not found",
                        HttpStatus.NOT_FOUND));

        Category parentCategory = null;

        if (categoryRequest.getParentId() != null) {
            Optional<Category> parentOpt = categoryRepository.findById(categoryRequest.getParentId());
            if (parentOpt.isEmpty()) {
                throw new CustomException(
                        "Parent category with ID " +
                                categoryRequest.getParentId() +
                                " not found",
                        HttpStatus.NOT_FOUND
                );
            }
            parentCategory = parentOpt.get();
        }

        if(categoryRequest.getName()!=null)
            category.setName(categoryRequest.getName());
        if(categoryRequest.getDescription()!=null)
            category.setDescription(categoryRequest.getDescription());

        category.setParent(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return "Category updated successfully";
    }

    public CategoryResponse getCategoryByCategoryId(Long id){

         Category category=categoryRepository.findById(id).orElseThrow(
                 ()->new CustomException("Category with ID " + id + " not found",
                         HttpStatus.NOT_FOUND));

          return categoryMapper.toResponse(category);
    }

    public List<CategoryResponse> getAllCategory(){

        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }


    public String deleteCategoryByCategoryId(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "Category with ID " + id + " not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        categoryRepository.delete(category);
        return "Category deleted successfully :"+ id;
    }
}
