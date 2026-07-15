package com.portfolio.orderservice.controller;

import com.portfolio.orderservice.dto.CreateOrderRequest;
import com.portfolio.orderservice.dto.OrderResponse;
import com.portfolio.orderservice.saga.OrderSagaOrchestrator;
import com.portfolio.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderSagaOrchestrator sagaOrchestrator;

    public OrderController(OrderService orderService, OrderSagaOrchestrator sagaOrchestrator) {
        this.orderService = orderService;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        // Nota: esta llamada es SÍNCRONA y BLOQUEANTE — el request HTTP del
        // cliente se mantiene abierto mientras corre por completo el SAGA (reservar +
        // cobrar, incluyendo la compensación si algo falla). El response ya
        // trae el status final: CONFIRMED o CANCELLED. En el Paso 3, cuando
        // Kafka reemplace estas llamadas HTTP por eventos, este endpoint va a
        // volverse asíncrono (responde 202 Accepted de inmediato, y el estado
        // se consulta después vía order-query-service).
        OrderResponse response = sagaOrchestrator.createOrderAndRunSaga(request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.listOrders();
    }
}