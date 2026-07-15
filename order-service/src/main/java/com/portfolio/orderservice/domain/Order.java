package com.portfolio.orderservice.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA: es una clase mutable con constructor vacío a propósito.
 * Un record no sirve aquí porque Hibernate necesita instanciar el objeto
 * "vacío" y rellenarlo por reflexión al leer de la base de datos, y además
 * necesita poder generar proxies para lazy-loading — ambas cosas son
 * incompatibles con la inmutabilidad de un record.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
        // Requerido por JPA/Hibernate, no usar directamente.
    }

    public Order(String customerId, List<OrderItem> items) {
        this.customerId = customerId;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
        this.items = new ArrayList<>();
        items.forEach(this::addItem);
        this.totalAmount = this.items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(OrderItem item) {
        item.assignTo(this);
        this.items.add(item);
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
