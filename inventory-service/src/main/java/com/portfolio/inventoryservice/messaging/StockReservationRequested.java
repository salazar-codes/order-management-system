package com.portfolio.inventoryservice.messaging;

import java.util.List;
import java.util.UUID;

/**
 * Copia local (a propósito) del mismo mensaje que publica order-service.
 * Mismo motivo que con los DTOs REST del Paso 1: cada servicio es dueño de
 * su propio contrato, no compartimos una clase Java entre ambos.
 */
public record StockReservationRequested(UUID orderId, List<ReservationItem> items) {

    public record ReservationItem(String sku, int quantity) {
    }
}