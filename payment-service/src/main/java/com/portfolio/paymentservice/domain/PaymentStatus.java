package com.portfolio.paymentservice.domain;

/**
 * COMPLETED: el cobro se procesó con éxito.
 * FAILED: el cobro fue rechazado (simulado); dispara la compensación en
 *         order-service (liberar el stock reservado).
 * REFUNDED: un cobro que sí se completó, pero luego se revirtió — hoy esto
 *           no lo dispara el SAGA (no reembolsamos tras confirmar), pero
 *           dejamos el estado listo para cuando el flujo lo necesite.
 */
public enum PaymentStatus {
    COMPLETED,
    FAILED,
    REFUNDED
}