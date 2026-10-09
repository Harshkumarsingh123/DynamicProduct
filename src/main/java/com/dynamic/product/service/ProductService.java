package com.dynamic.product.service;

import com.dynamic.product.dto.request.ProductFilterRequest;
import com.dynamic.product.dto.request.ProductRequest;
import com.dynamic.product.dto.response.ProductResponse;
import com.dynamic.product.entity.Category;
import com.dynamic.product.entity.Product;
import com.dynamic.product.exception.CustomException;
import com.dynamic.product.mapper.ProductMapper;
import com.dynamic.product.repository.CategoryRepository;
import com.dynamic.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.fileStorageService = fileStorageService;
    }

    public String saveProduct(ProductRequest productRequest) {

        Category category = categoryRepository
                .findById(productRequest.getCategoryId())
                .orElseThrow(() ->
                        new CustomException(
                                "Category with ID "
                                        + productRequest.getCategoryId()
                                        + " not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        Product product = new Product();

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());

        product.setVisible(
                productRequest.getVisible() != null
                        ? productRequest.getVisible()
                        : true
        );

        product.setCategory(category);
        product.setImageUrl(productRequest.getImageUrl());

        productRepository.save(product);

        return "Product created successfully";
    }

    public String updateProduct(Long id, ProductRequest productRequest) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "Product with ID " + id + " not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (productRequest.getName() != null) {
            product.setName(productRequest.getName());
        }

        if (productRequest.getDescription() != null) {
            product.setDescription(productRequest.getDescription());
        }

        if (productRequest.getPrice() != null) {
            product.setPrice(productRequest.getPrice());
        }

        if (productRequest.getVisible() != null) {
            product.setVisible(productRequest.getVisible());
        }

        if (productRequest.getCategoryId() != null) {

            Category category = categoryRepository
                    .findById(productRequest.getCategoryId())
                    .orElseThrow(() ->
                            new CustomException(
                                    "Category with ID "
                                            + productRequest.getCategoryId()
                                            + " not found",
                                    HttpStatus.NOT_FOUND
                            )
                    );

            product.setCategory(category);
        }

        if (productRequest.getImageUrl() != null) {
            product.setImageUrl(productRequest.getImageUrl());
        }

        productRepository.save(product);

        return "Product updated successfully";
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

    public String deleteProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "Product with ID " + id + " not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        productRepository.delete(product);

        return "Product deleted successfully";
    }

//    public List<ProductResponse> searchProducts(String keyword) {
//
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return getAllVisibleProduct();
//        }
//
//        return productRepository
//                .searchVisibleProducts(keyword)
//                .stream()
//                .map(productMapper::toResponse)
//                .toList();
//    }

    public Page<ProductResponse> getProducts(
            List<Long> categoryIds,
            Double minPrice,
            Double maxPrice,
            String keyword,
            String sortBy,
            String sortDirection,
            Integer page,
            Integer size) {

        boolean filterCategories =
                categoryIds != null && !categoryIds.isEmpty();
        if (!filterCategories) {
            categoryIds = List.of(-1L);
        } else {
            categoryIds = getCategoryAndChildren(categoryIds);
        }

        keyword = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();

        boolean filterKeyword = !keyword.isEmpty();

        Sort sort = getSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.filterProducts(
                filterCategories,
                categoryIds,
                minPrice,
                maxPrice,
                filterKeyword,
                keyword,
                pageable
        );

        return products.map(productMapper::toResponse);
    }
    private Sort getSort(String sortBy, String sortDirection) {

        if ("latest".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        if ("price".equalsIgnoreCase(sortBy)) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;

            return Sort.by(direction, "price");
        }

        if ("name".equalsIgnoreCase(sortBy)) {

            Sort.Direction direction =
                    "desc".equalsIgnoreCase(sortDirection)
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;

            return Sort.by(direction, "name");
        }

        return Sort.unsorted();
    }

    private List<Long> getCategoryAndChildren(List<Long> categoryIds) {

        List<Long> allCategoryIds = new ArrayList<>();
        for (Long categoryId : categoryIds) {

            allCategoryIds.add(categoryId);
            List<Category> children =
                    categoryRepository.findByParentId(categoryId);

            for (Category child : children) {
                allCategoryIds.add(child.getId());
            }
        }
        return allCategoryIds;
    }

}
