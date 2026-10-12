
package com.dynamic.product.product.controller;

import com.dynamic.product.common.response.ApiResponse;
import com.dynamic.product.product.dto.ProductRequest;
import com.dynamic.product.product.dto.ProductResponse;
import com.dynamic.product.product.service.ProductBulkUploadService;
import com.dynamic.product.product.service.ProductService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;
    private final ProductBulkUploadService productBulkUploadService;

    public ProductController(
            ProductService productService,
            ProductBulkUploadService productBulkUploadService) {
        this.productService = productService;
        this.productBulkUploadService = productBulkUploadService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createProduct(
            @Valid @RequestBody ProductRequest productRequest) {

        String message = productService.saveProduct(productRequest);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.CREATED.value(),
                message,
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest) {

        String message = productService.updateProduct(id, productRequest);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                message,
                null
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductById(
            @PathVariable Long id) {

        String message = productService.deleteProductById(id);

        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                message,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ProductResponse> products = productService.getProducts(
                categoryIds,
                minPrice,
                maxPrice,
                keyword,
                sortBy,
                sortDirection,
                page,
                size
        );

        ApiResponse<Page<ProductResponse>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Products fetched successfully",
                products
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/product-template")
    public ResponseEntity<Resource> downloadTemplate() throws IOException {

        Resource resource =
                new ClassPathResource("templates/ProductUploadTemplate.xlsx");

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product upload template not found"
            );
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ProductUploadTemplate.xlsx\""
                )
                .body(resource);
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadProducts(
            @RequestParam("file") MultipartFile file) throws IOException {

        Map<String, Object> uploadResult =
                productBulkUploadService.uploadProducts(file);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Bulk product upload processed",
                uploadResult
        );

        return ResponseEntity.ok(response);
    }
}
