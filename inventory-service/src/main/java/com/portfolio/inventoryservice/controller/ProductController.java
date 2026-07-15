package com.portfolio.inventoryservice.controller;

import com.portfolio.inventoryservice.dto.CreateProductRequest;
import com.portfolio.inventoryservice.dto.ProductResponse;
import com.portfolio.inventoryservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        return productService.getProduct(id);
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productService.listProducts();
    }
}
