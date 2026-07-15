package com.portfolio.inventoryservice.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String sku) {
        super("No se encontró el producto con sku " + sku);
    }
}
