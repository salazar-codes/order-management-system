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
        // 202 Accepted en vez de 201 Created: el pedido SÍ se creó, pero el
        // SAGA todavía no terminó (ni siquiera empezó a procesarse cuando
        // este método retorna). El status en el body va a decir CREATED —
        // hay que volver a consultar GET /api/orders/{id} para ver si llegó
        // a CONFIRMED o CANCELLED. Esta espera-sondeando ("polling") es
        // justo la incomodidad que order-query-service (Paso 5, CQRS) va a
        // resolver mejor.
        OrderResponse response = sagaOrchestrator.createOrderAndStartSaga(request);
        return ResponseEntity.accepted()
                .location(URI.create("/api/orders/" + response.id()))
                .body(response);
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