package com.portfolio.orderservice.domain;

/**
 * Hoy (Paso 1) solo usamos CREATED.
 * Los demás estados (STOCK_RESERVED, PAID, CONFIRMED, CANCELLED, COMPENSATING...)
 * los activaremos cuando construyamos el SAGA en el Paso 2 — no los adelantamos
 * para no modelar un flujo que todavía no existe.
 */
public enum OrderStatus {
    CREATED
}
