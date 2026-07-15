package com.portfolio.orderservice.exception;

/**
 * El orquestador la usa como señal de "inventory-service dijo que no hay
 * stock" (HTTP 409). Distinta de OrderNotFoundException: esta no es un error
 * de programación, es un resultado de negocio esperado que el SAGA maneja
 * cancelando el pedido.
 */
public class StockReservationException extends RuntimeException {
    public StockReservationException(String message) {
        super(message);
    }
}