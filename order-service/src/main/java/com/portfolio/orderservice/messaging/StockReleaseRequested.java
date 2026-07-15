package com.portfolio.orderservice.messaging;

import java.util.UUID;

/** Comando de compensación: "inventory-service, libera lo reservado para este pedido". */
public record StockReleaseRequested(UUID orderId) {
}