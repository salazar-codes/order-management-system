package com.portfolio.inventoryservice.dto;

/**
 * Respuesta de un chequeo RÁPIDO, no autoritativo, de disponibilidad. Un
 * "true" aquí no reserva nada — es solo una foto del momento. El único
 * chequeo que de verdad compromete stock sigue siendo reserveStock()
 * (Paso 2), consumido de forma asíncrona vía Kafka (Paso 3).
 */
public record AvailabilityResponse(boolean available) {
}