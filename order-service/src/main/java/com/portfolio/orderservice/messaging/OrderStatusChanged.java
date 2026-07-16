package com.portfolio.orderservice.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * El evento "para el mundo exterior": solo lo que alguien que quiere MOSTRAR
 * un pedido necesita, sin ningún detalle de cómo funciona el SAGA por
 * dentro. order-query-service (Paso 5) es su único consumidor hoy, pero
 * cualquier otro servicio de lectura futuro también podría suscribirse sin
 * tener que entender reservas de stock ni cobros.
 */
public record OrderStatusChanged(
        UUID orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        Instant updatedAt
) {
}