package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import com.duckchat.api.dto.ProductResponse;
import com.duckchat.api.entity.Product;
import com.duckchat.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "상품 목록을 성공적으로 가져왔습니다.", responses));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    ProductResponse response = ProductResponse.fromEntity(product);
                    return ResponseEntity.ok(new ApiResponse<>(true, "상품을 성공적으로 가져왔습니다.", response));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("%s 카테고리의 상품 목록을 성공적으로 가져왔습니다.", category), 
                responses));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("'%s' 검색 결과를 성공적으로 가져왔습니다.", keyword), 
                responses));
    }
}
