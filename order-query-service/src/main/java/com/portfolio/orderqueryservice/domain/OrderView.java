package com.portfolio.orderqueryservice.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * El modelo de LECTURA: plano, sin relaciones, sin lógica de negocio, sin
 * validaciones. No es una réplica de la entidad Order de order-service — es
 * exactamente lo que un cliente necesita para MOSTRAR un pedido, nada más.
 *
 * A diferencia de las entidades JPA (que vimos deben ser clases mutables con
 * constructor vacío por cómo Hibernate gestiona proxies y lazy-loading),
 * Spring Data R2DBC sí soporta records directamente: no hay proxies ni
 * lazy-loading en R2DBC, así que la inmutabilidad no choca con nada aquí.
 */
@Table("order_view")
public record OrderView(
        @Id UUID orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        Instant updatedAt
) {
}