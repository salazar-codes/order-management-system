package com.portfolio.orderservice.messaging;

import java.math.BigDecimal;
import java.util.UUID;

/** Comando: "payment-service, cobra este monto para este pedido". */
public record PaymentChargeRequested(UUID orderId, BigDecimal amount) {
}