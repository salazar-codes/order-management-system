package com.portfolio.orderservice.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * La tabla que hace posible el Outbox pattern. Guardamos aquí, en la MISMA
 * transacción que el Order, todo lo necesario para publicar después: topic,
 * key y el payload ya serializado a JSON. El OutboxPoller es el único que
 * lee esta tabla y sabe de Kafka — esta entidad no sabe nada de mensajería,
 * solo es un registro de "esto hay que publicar".
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String messageKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant publishedAt;

    protected OutboxEvent() {
    }

    public OutboxEvent(String topic, String messageKey, String payload) {
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}