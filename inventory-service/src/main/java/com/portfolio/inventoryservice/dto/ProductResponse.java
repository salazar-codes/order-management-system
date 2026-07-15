package com.portfolio.inventoryservice.dto;

import com.portfolio.inventoryservice.domain.Product;

import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        int quantityAvailable,
        int quantityReserved
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getQuantityAvailable(),
                product.getQuantityReserved()
        );
    }
}
