package com.portfolio.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveItemRequest(
        @NotBlank String sku,
        @Min(1) int quantity
) {
}