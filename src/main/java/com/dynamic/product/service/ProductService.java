package com.dynamic.product.service;

import com.dynamic.product.dto.request.ProductRequest;
import com.dynamic.product.dto.response.ProductResponse;
import com.dynamic.product.entity.Category;
import com.dynamic.product.entity.Product;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.mapper.ProductMapper;
import com.dynamic.product.repository.CategoryRepository;
import com.dynamic.product.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }


    public ProductResponse saveProduct(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(
                () -> new CustomException("Invalid Category Id", HttpStatus.NOT_FOUND));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImageUrl(productRequest.getImageUrl());
        product.setVisible(productRequest.getVisible());
        product.setCategory(category);

        Product saveProduct = productRepository.save(product);
        return productMapper.toResponse(saveProduct);
    }


    public ProductResponse updateProduct(Long id,ProductRequest productRequest) {

        Product product=productRepository.findById(id).orElseThrow(
                ()->new CustomException("Invalid Product Id",HttpStatus.NOT_FOUND));

        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(
                () -> new CustomException("Invalid Category Id",HttpStatus.NOT_FOUND));


        if(productRequest.getName()!=null)
            product.setName(productRequest.getName());
        if(productRequest.getDescription()!=null)
            product.setDescription(productRequest.getDescription());
        if(productRequest.getPrice()!=0)
            product.setPrice(productRequest.getPrice());
        if(productRequest.getImageUrl()!=null)
            product.setImageUrl(productRequest.getImageUrl());
            product.setVisible(productRequest.getVisible());
            product.setCategory(category);

        Product saveProduct = productRepository.save(product);
        return productMapper.toResponse(saveProduct);
    }

    public List<ProductResponse> getAllVisibleProduct(){
        return productRepository.findByVisibleTrue()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public List<ProductResponse> getProductByCategoryId(Long id) {

        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new CustomException("Invalid Category Id",HttpStatus.NOT_FOUND));

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
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {

        Product product = productRepository
                .findByIdAndVisibleTrue(id)
                .orElseThrow(() ->
                        new CustomException(
                                "Product not found with id: " + id,HttpStatus.NOT_FOUND
                        ));

        return productMapper.toResponse(product);
    }

    public void deleteProductById(Long id){
        try {
             productRepository.deleteById(id);
        }
        catch( CustomException exception){
                throw new CustomException("Invalid Product Id",HttpStatus.NOT_FOUND);
        }
    }

    public List<ProductResponse> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVisibleProduct();
        }

        return productRepository
                .searchVisibleProducts(keyword)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

}
