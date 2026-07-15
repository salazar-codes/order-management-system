package com.portfolio.paymentservice.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Payment() {
    }

    public Payment(UUID orderId, BigDecimal amount, PaymentStatus status) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
