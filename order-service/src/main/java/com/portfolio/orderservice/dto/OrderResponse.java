package com.portfolio.orderservice.dto;

import com.portfolio.orderservice.domain.Order;
import com.portfolio.orderservice.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    // Mapper estático simple. Cuando el proyecto crezca podríamos usar MapStruct,
    // pero para un CRUD de este tamaño un método estático es más fácil de leer
    // y depurar que generar un mapper con anotaciones.
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }

    public record OrderItemResponse(String productSku, int quantity, BigDecimal unitPrice) {
        public static OrderItemResponse from(com.portfolio.orderservice.domain.OrderItem item) {
            return new OrderItemResponse(item.getProductSku(), item.getQuantity(), item.getUnitPrice());
        }
    }
}
