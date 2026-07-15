package com.portfolio.inventoryservice.controller;

import com.portfolio.inventoryservice.dto.ReserveStockRequest;
import com.portfolio.inventoryservice.service.StockReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller separado de ProductController: /api/products es CRUD de
 * catálogo, /api/inventory es el "protocolo" que usa el SAGA para
 * reservar/liberar. Mezclar ambos en un solo controller confundiría dos
 * responsabilidades distintas.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final StockReservationService reservationService;

    public InventoryController(StockReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody ReserveStockRequest request) {
        reservationService.reserveStock(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release/{orderId}")
    public ResponseEntity<Void> release(@PathVariable UUID orderId) {
        reservationService.releaseStock(orderId);
        return ResponseEntity.ok().build();
    }
}