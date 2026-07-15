package com.portfolio.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank String productSku,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPrice
) {
}
