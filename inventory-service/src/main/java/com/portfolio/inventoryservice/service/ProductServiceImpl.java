package com.portfolio.inventoryservice.service;

import com.portfolio.inventoryservice.domain.Product;
import com.portfolio.inventoryservice.dto.CreateProductRequest;
import com.portfolio.inventoryservice.dto.ProductResponse;
import com.portfolio.inventoryservice.exception.ProductNotFoundException;
import com.portfolio.inventoryservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product(request.sku(), request.name(), request.initialStock());
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));
        return ProductResponse.from(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    // Nota deliberada: NO hay método reserveStock()/releaseStock() todavía.
    // Esos son justamente los pasos del SAGA que vamos a agregar en el Paso 2,
    // cuando inventory-service empiece a reaccionar a eventos de Kafka en vez
    // de solo servir un CRUD.

    @Override
    @Transactional(readOnly = true)
    public boolean checkAvailability(String sku, int quantity) {
        return productRepository.findBySku(sku)
                .map(product -> product.getQuantityAvailable() >= quantity)
                .orElse(false); // si el producto ni siquiera existe, "no disponible"
    }
}
