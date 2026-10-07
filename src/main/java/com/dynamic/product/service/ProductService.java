package com.dynamic.product.service;

import com.dynamic.product.dto.ProductRequest;
import com.dynamic.product.dto.ProductResponse;
import com.dynamic.product.entity.Category;
import com.dynamic.product.entity.Product;
import com.dynamic.product.exception.CategoryNotFoundException;
import com.dynamic.product.exception.ProductNotFoundException;
import com.dynamic.product.repository.CategoryRepository;
import com.dynamic.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    public ProductResponse saveProduct(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategory_id()).orElseThrow(
                () -> new CategoryNotFoundException("Invalid Category Id"));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImageUrl(productRequest.getImageUrl());
        product.setVisible(productRequest.isVisible());
        product.setCategory(category);

        Product saveProduct = productRepository.save(product);
        return mapToResponse(saveProduct);
    }


    public ProductResponse updateProduct(Long id,ProductRequest productRequest) {

        Product product=productRepository.findById(id).orElseThrow(
                ()->new ProductNotFoundException("Invalid Product Id"));

        Category category = categoryRepository.findById(productRequest.getCategory_id()).orElseThrow(
                () -> new CategoryNotFoundException("Invalid Category Id"));


        if(productRequest.getName()!=null)
            product.setName(productRequest.getName());
        if(productRequest.getDescription()!=null)
            product.setDescription(productRequest.getDescription());
        if(productRequest.getPrice()!=0)
            product.setPrice(productRequest.getPrice());
        if(productRequest.getImageUrl()!=null)
            product.setImageUrl(productRequest.getImageUrl());
            product.setVisible(productRequest.isVisible());
            product.setCategory(category);

        Product saveProduct = productRepository.save(product);
        return mapToResponse(saveProduct);
    }

    public List<ProductResponse> getAllVisibleProduct(){
        return productRepository.findByVisibleTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> getProductByCategoryId(Long id) {

        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Invalid Category Id"));

        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(category.getId());

        List<Category> subCategories =
                categoryRepository.findByParentId(category.getId());

        categoryIds.addAll(
                subCategories.stream()
                        .map(Category::getId)
                        .toList()
        );

        return productRepository
                .findByCategoryIdInAndVisibleTrue(categoryIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {

        Product product = productRepository
                .findByIdAndVisibleTrue(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        ));

        return mapToResponse(product);
    }

    public void deleteProductById(Long id){
        try {
             productRepository.deleteById(id);
        }
        catch( ProductNotFoundException exception){
                throw new ProductNotFoundException("Invalid Product Id");
        }
    }
    public ProductResponse mapToResponse(Product product){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }

    public List<ProductResponse> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVisibleProduct();
        }

        return productRepository
                .searchVisibleProducts(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

}
