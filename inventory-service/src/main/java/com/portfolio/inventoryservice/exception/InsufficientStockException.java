package com.portfolio.inventoryservice.exception;

import java.util.List;

/**
 * Se lanza ANTES de mutar ningún Product, tras validar todos los items del
 * pedido. Así garantizamos que una reserva es "todo o nada": si un solo sku
 * no tiene stock suficiente, no reservamos ninguno de los otros items.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(List<String> details) {
        super("Stock insuficiente para: " + String.join("; ", details));
    }
}