package com.portfolio.orderservice.messaging;

import java.util.UUID;

/** Evento: respuesta de inventory-service a un StockReservationRequested. */
public record StockReservationReplied(UUID orderId, boolean approved, String reason) {
}