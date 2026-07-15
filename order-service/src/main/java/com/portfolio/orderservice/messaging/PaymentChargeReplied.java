package com.portfolio.orderservice.messaging;

import java.util.UUID;

/** Evento: respuesta de payment-service a un PaymentChargeRequested. */
public record PaymentChargeReplied(UUID orderId, boolean approved, String reason) {
}