package com.portfolio.orderservice.controller;

import com.portfolio.orderservice.dto.CreateOrderRequest;
import com.portfolio.orderservice.dto.OrderResponse;
import com.portfolio.orderservice.saga.OrderSagaOrchestrator;
import com.portfolio.orderservice.saga.StockAvailabilityValidator;
import com.portfolio.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final StockAvailabilityValidator stockAvailabilityValidator;

    public OrderController(OrderService orderService,
                           OrderSagaOrchestrator sagaOrchestrator,
                           StockAvailabilityValidator stockAvailabilityValidator) {
        this.orderService = orderService;
        this.sagaOrchestrator = sagaOrchestrator;
        this.stockAvailabilityValidator = stockAvailabilityValidator;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest request) {
        // Pre-chequeo síncrono (Paso 6), protegido con circuit breaker. Si
        // inventory-service dice que no hay stock, O si el circuito está
        // abierto (inventory-service viene fallando), rechazamos de una vez
        // sin arrancar el SAGA. No es la validación autoritativa — es solo
        // para no hacer trabajo async de más cuando ya se ve venir el fallo.
        if (!stockAvailabilityValidator.allItemsLikelyAvailable(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message",
                            "Uno o más productos no tienen stock suficiente, o inventory-service "
                                    + "no está respondiendo. Pedido no creado."));
        }

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