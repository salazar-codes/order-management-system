package com.portfolio.orderservice.messaging;

import java.util.List;
import java.util.UUID;

/** Comando: "inventory-service, reserva estos items para este pedido". */
public record StockReservationRequested(UUID orderId, List<ReservationItem> items) {

    public record ReservationItem(String sku, int quantity) {
    }
}