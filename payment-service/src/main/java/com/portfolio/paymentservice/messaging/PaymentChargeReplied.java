package com.portfolio.paymentservice.messaging;

import java.util.UUID;

public record PaymentChargeReplied(UUID orderId, boolean approved, String reason) {
}