package com.portfolio.inventoryservice.domain;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Modelamos el stock como dos contadores separados en vez de uno solo:
 * - quantityAvailable: lo que se puede vender ahora mismo.
 * - quantityReserved: lo que está "apartado" para pedidos en curso del SAGA.
 * Esta separación es la que en el Paso 2 nos permite reservar sin descontar
 * definitivamente, y liberar (compensar) si el pedido falla más adelante.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantityAvailable;

    @Column(nullable = false)
    private int quantityReserved;

    protected Product() {
    }

    public Product(String sku, String name, int quantityAvailable) {
        this.sku = sku;
        this.name = name;
        this.quantityAvailable = quantityAvailable;
        this.quantityReserved = 0;
    }

    public void increaseStock(int amount) {
        this.quantityAvailable += amount;
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }
}