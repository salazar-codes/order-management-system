package com.portfolio.paymentservice.exception;

/**
 * Se lanza cuando el "proveedor de pago" (simulado) rechaza el cobro.
 * order-service la traduce a un 402 y dispara la compensación de inventario.
 */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}