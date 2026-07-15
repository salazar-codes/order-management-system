package com.portfolio.orderservice.client;

import java.util.List;
import java.util.UUID;

/**
 * Nota deliberada: este record está duplicado (con otro nombre de paquete)
 * en inventory-service. Es una duplicación aceptable en microservicios: cada
 * servicio es dueño de su propio contrato, y no compartimos una librería
 * común de DTOs porque eso acoplaría el build de ambos servicios entre sí
 * (justo lo que "database per service" e independencia de despliegue buscan
 * evitar).
 */
public record ReserveStockRequest(UUID orderId, List<ReserveItemRequest> items) {
}