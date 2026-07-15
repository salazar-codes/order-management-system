package com.portfolio.paymentservice.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(UUID id) {
        super("No se encontró el pago con id " + id);
    }
}
