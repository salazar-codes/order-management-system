package com.portfolio.paymentservice.domain;

/**
 * Hoy solo usamos COMPLETED al crear un pago (simulamos que siempre se cobra
 * con éxito). FAILED y REFUNDED entran en el Paso 2, cuando el SAGA necesite
 * simular fallos de pago para disparar la compensación de inventario.
 */
public enum PaymentStatus {
    COMPLETED
}
