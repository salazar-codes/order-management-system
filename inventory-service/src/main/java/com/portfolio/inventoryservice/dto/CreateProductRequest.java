package com.portfolio.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @Min(0) int initialStock
) {
}
