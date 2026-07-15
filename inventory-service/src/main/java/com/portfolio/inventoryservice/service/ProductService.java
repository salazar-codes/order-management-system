package com.portfolio.inventoryservice.service;

import com.portfolio.inventoryservice.dto.CreateProductRequest;
import com.portfolio.inventoryservice.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProduct(UUID id);

    List<ProductResponse> listProducts();
}
