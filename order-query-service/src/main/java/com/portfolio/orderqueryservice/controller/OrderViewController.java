package com.portfolio.orderqueryservice.controller;

import com.portfolio.orderqueryservice.domain.OrderView;
import com.portfolio.orderqueryservice.repository.OrderViewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Nótese: NINGÚN .block() aquí. Todo el camino HTTP -> repositorio -> base
 * de datos es no bloqueante de punta a punta. Devolver un Mono/Flux directo
 * desde un @GetMapping es suficiente — Spring WebFlux se suscribe por ti
 * cuando la respuesta HTTP está lista para enviarse.
 */
@RestController
@RequestMapping("/api/order-views")
public class OrderViewController {

    private final OrderViewRepository orderViewRepository;

    public OrderViewController(OrderViewRepository orderViewRepository) {
        this.orderViewRepository = orderViewRepository;
    }

    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<OrderView>> getById(@PathVariable UUID orderId) {
        return orderViewRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<OrderView> list() {
        return orderViewRepository.findAll();
    }
}