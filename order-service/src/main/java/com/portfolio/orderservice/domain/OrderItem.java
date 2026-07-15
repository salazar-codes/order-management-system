package com.portfolio.orderservice.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private String productSku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    protected OrderItem() {
    }

    public OrderItem(String productSku, int quantity, BigDecimal unitPrice) {
        this.productSku = productSku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void assignTo(Order order) {
        this.order = order;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() {
        return id;
    }

    public String getProductSku() {
        return productSku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
