package com.portfolio.orderservice.service;

import com.portfolio.orderservice.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

/**
 * Deliberadamente solo de lectura ahora: crear un pedido ya no es un simple
 * "guardar en la base" (Paso 1), es correr un SAGA completo — esa
 * responsabilidad vive en OrderSagaOrchestrator, no aquí. Mezclar ambas cosas
 * en esta clase la haría manejar dos niveles de complejidad muy distintos.
 */
public interface OrderService {

    OrderResponse getOrder(UUID id);

    List<OrderResponse> listOrders();
}