package com.portfolio.orderservice.client;

/** Espejo del DTO que espera inventory-service en POST /api/inventory/reserve. */
public record ReserveItemRequest(String sku, int quantity) {
}