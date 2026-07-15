package com.portfolio.inventoryservice.messaging;

import java.util.UUID;

public record StockReleaseRequested(UUID orderId) {
}