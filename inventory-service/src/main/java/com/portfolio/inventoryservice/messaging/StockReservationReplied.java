package com.portfolio.inventoryservice.messaging;

import java.util.UUID;

public record StockReservationReplied(UUID orderId, boolean approved, String reason) {
}