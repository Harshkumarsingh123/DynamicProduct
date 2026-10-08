package com.dynamic.product.controller;

import com.dynamic.product.dto.request.ProductRequest;
import com.dynamic.product.dto.response.ApiResponse;
import com.dynamic.product.dto.response.ProductResponse;
import com.dynamic.product.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct( @Valid
                                                      @RequestBody ProductRequest productRequest){

        String message=productService.saveProduct(productRequest);
        ApiResponse response = new ApiResponse(
                HttpStatus.CREATED.value(),
                message,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PutMapping(value = "/{id}")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id,
                                                     @Valid @RequestBody ProductRequest productRequest){


        String message = productService.updateProduct(id, productRequest);
        ApiResponse response = new ApiResponse(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<ProductResponse>> getVisibleProduct() {

        List<ProductResponse> productResponses =
                productService.getAllVisibleProduct();

        return ResponseEntity.ok(productResponses);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {

        ProductResponse productResponse =
                productService.getProductById(id);

        return ResponseEntity.ok(productResponse);
    }


    @GetMapping("/category/{id}")
    public ResponseEntity<List<ProductResponse>> getProductByCategoryId(@PathVariable Long id) {

        List<ProductResponse> productResponses =
                productService.getProductByCategoryId(id);

        return ResponseEntity.ok(productResponses);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProductById(@PathVariable Long id) {

        String message = productService.deleteProductById(id);

        ApiResponse response = new ApiResponse(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {

        List<ProductResponse> products =
                productService.searchProducts(keyword);

        return ResponseEntity.ok(products);
    }
}