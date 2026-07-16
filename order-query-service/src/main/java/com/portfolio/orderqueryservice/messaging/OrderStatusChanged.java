package com.portfolio.orderqueryservice.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Copia local del mismo evento que publica order-service (mismo motivo de siempre: cada servicio es dueño de su propio contrato). */
public record OrderStatusChanged(
        UUID orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        Instant updatedAt
) {
}