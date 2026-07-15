package com.portfolio.paymentservice.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentChargeRequested(UUID orderId, BigDecimal amount) {
}