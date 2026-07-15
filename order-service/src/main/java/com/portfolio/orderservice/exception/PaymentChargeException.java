package com.portfolio.orderservice.exception;

/**
 * Señal de "payment-service rechazó el cobro" (HTTP 402). El orquestador la
 * usa para decidir que hay que compensar (liberar el stock reservado).
 */
public class PaymentChargeException extends RuntimeException {
    public PaymentChargeException(String message) {
        super(message);
    }
}