package com.portfolio.orderservice.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID id) {
        super("No se encontró el pedido con id " + id);
    }
}
