package com.portfolio.inventoryservice.domain;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Registra exactamente qué se reservó para qué pedido. Sin esta tabla,
 * "liberar stock" no sabría CUÁNTO liberar de cada producto — tendríamos que
 * confiar en que order-service nos reenvíe las cantidades correctas, lo cual
 * es fragil. Guardando la reserva aquí, release(orderId) es autosuficiente:
 * solo necesita el orderId para saber qué deshacer.
 */
@Entity
@Table(name = "stock_reservations")
public class StockReservation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String productSku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean active;

    protected StockReservation() {
    }

    public StockReservation(UUID orderId, String productSku, int quantity) {
        this.orderId = orderId;
        this.productSku = productSku;
        this.quantity = quantity;
        this.active = true;
    }

    public void release() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getProductSku() {
        return productSku;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isActive() {
        return active;
    }
}