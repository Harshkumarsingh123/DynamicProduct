package com.dynamic.product.controller;

import com.dynamic.product.dto.request.ProductRequest;
import com.dynamic.product.dto.response.ProductResponse;
import com.dynamic.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> saveProduct(@Valid @RequestBody ProductRequest productRequest){
        ProductResponse productResponse=productService.saveProduct(productRequest);
        return new ResponseEntity<>(productResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest){
        ProductResponse productResponse=productService.updateProduct(id,productRequest);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getVisibleProduct(){
        List<ProductResponse> productResponses=productService.getAllVisibleProduct();
        return ResponseEntity.ok(productResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id){
        ProductResponse productResponse=productService.getProductById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<ProductResponse>> getProductByCategoryId(@PathVariable Long id){
        List<ProductResponse> productResponses=productService.getProductByCategoryId(id);
        return ResponseEntity.ok(productResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id){
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String keyword) {

        List<ProductResponse> products =
                productService.searchProducts(keyword);

        return ResponseEntity.ok(products);
    }

}
