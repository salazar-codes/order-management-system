package com.portfolio.orderservice.domain;

/**
 * CREATED: el pedido se guardó localmente, el SAGA todavía no arrancó.
 * STOCK_RESERVED: inventory-service confirmó la reserva; vamos a intentar cobrar.
 * CONFIRMED: el SAGA completo tuvo éxito (fin feliz).
 * CANCELLED: algún paso falló y ya se aplicó la compensación correspondiente
 *            (o no hizo falta compensar nada, si falló el primer paso).
 */
public enum OrderStatus {
    CREATED,
    STOCK_RESERVED,
    CONFIRMED,
    CANCELLED
}