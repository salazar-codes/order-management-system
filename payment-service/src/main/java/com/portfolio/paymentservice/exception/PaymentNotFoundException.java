package com.portfolio.paymentservice.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(UUID id) {
        super("No se encontró el pago con id " + id);
    }

    public static PaymentNotFoundException forOrder(UUID orderId) {
        return new PaymentNotFoundException("No se encontró un pago COMPLETED para el pedido " + orderId);
    }

    private PaymentNotFoundException(String rawMessage) {
        super(rawMessage);
    }
}